package com.bubble.crawler.job.zk.watch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;

/**
 * 实现Watcher接口，接收ZNode的变化通知
 * 执行：./executor.sh localhost:2181 /watch data/znode-data seq.sh
 *
 * @author wugang
 * date: 2020-04-13 18:56
 **/
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {
    private String zNode;
    private DataMonitor dataMonitor;
    private ZooKeeper zk;
    private String pathname;
    private String exec[];
    private Process child;

    /**
     * 初始化执行的构造函数
     *
     * @param hostPort zk连接串，如：localhost:2181
     * @param zNode    要监听的ZNode路径
     * @param filename 接收znode数据的文件路径
     * @param exec     要启动的进程
     * @throws KeeperException
     * @throws IOException
     */
    public Executor(String hostPort, String zNode, String filename, String exec[]) throws KeeperException, IOException {
        this.pathname = filename;
        this.exec = exec;
        // 创建zk实例
        zk = new ZooKeeper(hostPort, 3000, this);
        dataMonitor = new DataMonitor(zk, zNode, this);
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("USAGE: Executor hostPort zNode pathname program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String zNode = args[1];
        String filename = args[2];
        String exec[] = new String[args.length - 3];
        System.arraycopy(args, 3, exec, 0, exec.length);
        try {
            new Executor(hostPort, zNode, filename, exec).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        // ZNode发生变化时，这个方法会被调用执行
        dataMonitor.handle(watchedEvent);
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void exists(byte[] data) {
        // 如当前获取的数据为空，将已经启动的线程kill掉
        if (data == null) {
            if (child != null) {
                System.out.println("Killing handle");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        } else {
            // 当前获取的数据不为空，下面就启动一个线程来执行，把znode的数据读取出来，保存在文件中
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(pathname));
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    static class StreamWriter extends Thread {
        OutputStream os;
        InputStream is;

        StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
            }
        }
    }

}
