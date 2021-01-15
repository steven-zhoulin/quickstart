import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Steven
 * @date 2021-01-04
 */
public class Main {
    public static void main(String[] args) {
        PriorityBlockingQueue<Long> queue = new PriorityBlockingQueue<>(10);
        for (long i = 0; i < 11; i++) {
            queue.offer(i);
            System.out.println("offer " + i);
        }
    }
}
