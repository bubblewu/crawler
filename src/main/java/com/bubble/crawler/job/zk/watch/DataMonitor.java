package com.bubble.crawler.job.zk.watch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

/**
 * 执行zk的异步API：
 * 客户端异步接收来自服务端的响应，调用者实现DataMonitorListener接口即可
 *
 * @author wugang
 * date: 2020-04-13 18:56
 **/
public class DataMonitor implements AsyncCallback.StatCallback {
    private ZooKeeper zk;
    private String zNode;
    boolean dead; // 服务是否dead
    private DataMonitorListener listener;
    private byte prevData[]; // 上一个节点的版本数据

    public DataMonitor(ZooKeeper zk, String zNode, DataMonitorListener listener) {
        this.zk = zk;
        this.zNode = zNode;
        this.listener = listener;
        // 首先检查节点是否存在
        zk.exists(zNode, true, this, null);
    }

    /**
     * 判断监听器事件的各种类型，执行对应操作
     *
     * @param event 监听器事件
     */
    public void handle(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Watcher.Event.EventType.None) {
            // 连接的状态已经改变
            switch (event.getState()) {
                case SyncConnected:
                    // 不需做任何处理，watcher会自动注册到zk服务器
                    // 当客户端断开连接时触发的任何watcher操作都会被按顺序发送
                    break;
                case Expired:
                    // 会话过期，当前客户端连接的session失效，
                    // 如要继续访问zk集群，需要重写实例化连接
                    dead = true;
                    listener.closing(Code.SESSIONEXPIRED.intValue());
                    break;
            }
        } else {
            // ZNode路径不为空，且是我们关注的ZNode
            if (path != null && path.equals(zNode)) {
                // 节点上发生了变化，查看ZNode的状态，（异步）
                // 其中this就是callback，后面就会执行processResult
                zk.exists(zNode, true, this, null);
            }
        }
    }

    /**
     * 对服务器端的返回值进行判断
     *
     * @param rc 服务器的状态码
     * @param path 路径
     * @param ctx
     * @param stat stat信息结构体
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc == Code.OK.intValue()) {
            pro(true);
        } else if (rc == Code.NONODE.intValue()) {
            pro(false);
        } else if (rc == Code.SESSIONEXPIRED.intValue() || rc == Code.NOAUTH.intValue()) {
            // 会话过期或无权限时，关闭
            dead = true;
            listener.closing(rc);
            return;
        } else {
            // Retry errors
            zk.exists(zNode, true, this, null);
            return;
        }
    }

    private void pro(boolean exists) {
        byte b[] = null;
        if (exists) { // 如节点存在，获取节点下的数据
            try {
                b = zk.getData(zNode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        // 当前获取的数据不为空，且和以前不一样。
        if ((b == null && b != prevData) || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            // 将当前版本的数据过期，作为新的老版本数据
            prevData = b;
        }
    }

    /**
     * 他类通过实现这个方法来使用DataMonitor
     */
    public interface DataMonitorListener {
        /**
         * 是否存在
         *
         * @param data 节点下的数据
         */
        void exists(byte data[]);

        /**
         * 关闭
         *
         * @param rc watcher返回的状态码
         */
        void closing(int rc);
    }

}
