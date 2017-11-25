package org.pdlp.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by zgh on 2017/11/25.
 */
public class AIOClient implements Runnable {

    private AsynchronousSocketChannel client;
    private String host;
    private int port;

    public AIOClient(String host, int port) throws IOException {
        this.client = AsynchronousSocketChannel.open();
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {
        try {
            new Thread(new AIOClient("127.0.0.1", 8888)).start();
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        client.connect(new InetSocketAddress(host, port), null, new CompletionHandler<Void, Object>() {
            @Override
            public void completed(Void result, Object attachment) {
                String msg = "client test msg-" + Math.random();
                client.write(ByteBuffer.wrap(msg.getBytes()));
                System.out.println("client send data: " + msg);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("client send field...");
            }
        });

        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        client.read(byteBuffer, this, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                System.out.println(result);
                System.out.println("client read data: " + new String(byteBuffer.array()));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("read failed");
            }
        });
    }
}
