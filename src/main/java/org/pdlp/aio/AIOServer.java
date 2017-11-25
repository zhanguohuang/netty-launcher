package org.pdlp.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

public class AIOServer implements Runnable {

    private int port = 8888;
    private int threadSize = 10;
    protected AsynchronousChannelGroup asynchronousChannelGroup;

    protected AsynchronousServerSocketChannel serverChannel;

    public AIOServer(int port, int threadSize) {
        this.port = port;
        this.threadSize = threadSize;
    }

    public static void main(String[] args) throws IOException {
        try {
            new Thread(new AIOServer(8888, 20)).start();
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            asynchronousChannelGroup = AsynchronousChannelGroup.withCachedThreadPool(
                    Executors.newCachedThreadPool(), threadSize);
            serverChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            serverChannel.bind(new InetSocketAddress(port));
            System.out.println("listening on port: " + port);
            serverChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, AIOServer>() {
                final ByteBuffer echoBuffer = ByteBuffer.allocateDirect(1024);
                @Override
                public void completed(AsynchronousSocketChannel result, AIOServer attachment) {
                    System.out.println("reading begin...");
                    try {
                        System.out.println("远程地址: " +  result.getRemoteAddress());
                        echoBuffer.clear();
                        result.read(echoBuffer).get();
                        echoBuffer.flip();
                        System.out.println("received: " + Charset.defaultCharset().decode(echoBuffer));
                        String msg  = "server test msg-" + Math.random();
                        System.out.println("server send data: " + msg);
                        result.write(ByteBuffer.wrap(msg.getBytes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        attachment.serverChannel.accept(attachment, this);
                    }
                }

                @Override
                public void failed(Throwable exc, AIOServer attachment) {
                    System.out.println("received failed");
                    exc.printStackTrace();
                    attachment.serverChannel.accept(attachment, this);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
