import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Simulacija {

    static final int MAX_TIME = 5000;
    static final Random rand = new Random();
    static final List<Double> allScores = Collections.synchronizedList(new ArrayList<>());
    static final long start = System.currentTimeMillis();

    static final Semaphore assistantSemaphore = new Semaphore(1);
    static final BlockingQueue<Student> professorQueue = new LinkedBlockingQueue<>();
    static final CyclicBarrier profBarrier = new CyclicBarrier(2);
    static final ExecutorService pool = Executors.newCachedThreadPool();
    static final AtomicInteger studentCounter = new AtomicInteger();
    static final AtomicInteger missedStudents = new AtomicInteger(); // broj studenata koji nisu stigli na vreme
    static CountDownLatch latch; // za čekanje na završetak svih studenata

    public static void main(String[] args) {
        int N = 13; // broj studenata
        latch = new CountDownLatch(N); // inicijalizujemo latch sa brojem studenata

        // pokretanje profesorskih niti
        pool.execute(() -> professorWorker());
        pool.execute(() -> professorWorker());

        for (int i = 0; i < N; i++) {
            final int id = i;
            pool.execute(() -> {
                try {
                    Thread.sleep(rand.nextInt(1000)); // kasni do 1s
                    long arrival = System.currentTimeMillis() - start;

                    // Provera da li je student zakasnio
                    if (arrival > MAX_TIME) {
                        missedStudents.incrementAndGet(); // Student missed the deadline
                        latch.countDown(); // završi čekanje za ovog studenta
                        return; // student nije stigao
                    }

                    Student s = new Student("Student-" + id, arrival);

                    // Ako student ide kod asistenta
                    if (rand.nextBoolean()) {
                        handleAssistant(s);
                    } else {
                        // Ako student ide kod profesora
                        professorQueue.offer(s);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // Zajednička tačka - završi čekanje za ovog studenta
                }
            });
        }

        // čekaj da se svi studenti obrade
        try {
            latch.await(); // čekaj dok svi studenti ne završe
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pool.shutdown();
        try {
            pool.awaitTermination(MAX_TIME + 2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double avg = allScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        System.out.printf("Average Score: %.2f (%d students)%n", avg, allScores.size());
        System.out.printf("Missed students: %d%n", missedStudents.get()); // Ispis broja studenata koji nisu stigli na vreme
    }

    static void handleAssistant(Student s) {
        try {
            if (!assistantSemaphore.tryAcquire()) return;

            long now = System.currentTimeMillis();
            if (now - start > MAX_TIME) {
                missedStudents.incrementAndGet(); // Student missed the deadline
                return; // Student je zakasnio
            }

            long ttc = 500 + rand.nextInt(501);
            if (now + ttc - start > MAX_TIME) {
                missedStudents.incrementAndGet(); // Student missed the deadline
                return; // Student je zakasnio
            }

            Thread.sleep(ttc);
            int score = 5 + rand.nextInt(6);
            allScores.add((double) score);
            studentCounter.incrementAndGet();

            printLog(s, "Assistant", ttc, now - start, score);
        } catch (InterruptedException ignored) {
        } finally {
            assistantSemaphore.release();
        }
    }

    static void professorWorker() {
        while (System.currentTimeMillis() - start < MAX_TIME) {
            try {
                Student s1 = professorQueue.poll(100, TimeUnit.MILLISECONDS);
                if (s1 == null) continue;

                // čekamo da se pojavi drugi student
                Student s2 = professorQueue.poll(MAX_TIME - (System.currentTimeMillis() - start), TimeUnit.MILLISECONDS);
                if (s2 == null) {
                    // vreme isteklo, vrati s1
                    professorQueue.offer(s1);
                    continue;
                }

                // kad imamo 2 studenta, sinhronizujemo početak
                profBarrier.await();

                long now = System.currentTimeMillis();
                long ttc1 = 500 + rand.nextInt(501);
                long ttc2 = 500 + rand.nextInt(501);
                long maxTTC = Math.max(ttc1, ttc2);

                if (now + maxTTC - start > MAX_TIME) {
                    missedStudents.incrementAndGet(); // Prof missed the deadline
                    latch.countDown(); // završi čekanje za oba studenta
                    return;
                }

                Thread.sleep(maxTTC);

                int score1 = 5 + rand.nextInt(6);
                int score2 = 5 + rand.nextInt(6);

                allScores.add((double) score1);
                allScores.add((double) score2);
                studentCounter.addAndGet(2);

                printLog(s1, "Professor", ttc1, now - start, score1);
                printLog(s2, "Professor", ttc2, now - start, score2);

                latch.countDown(); // završi čekanje za oba studenta

            } catch (Exception ignored) {
            }
        }
    }

    static void printLog(Student s, String who, double ttc, double startTime, int score) {
        System.out.printf("Thread: %s Arrival: %.2fms Prof: %s TTC: %.2fms:%.2fms Score: %d%n",
                s.name, s.arrivalTime, who, ttc, startTime, score);
    }

    static class Student {
        String name;
        double arrivalTime;

        Student(String name, double arrivalTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
        }
    }

}