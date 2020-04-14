package com.bubble.crawler.job.zk.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * 处理ZNode节点数据：
 * 表示一个不可变的临时znode名称，该名称具有一个有序序号，并且可以按顺序排序。
 * znode的期望名称格式如下:
 * <pre>
 * &lt;name&gt;-&lt;sequence&gt;
 *
 * For example: lock-00001
 * </pre>
 */
class ZNodeName implements Comparable<ZNodeName> {
    private static final Logger LOG = LoggerFactory.getLogger(ZNodeName.class);

    private final String name;
    private final String prefix; // 节点的前缀名
    private final Optional<Integer> sequence; // 节点的顺序号，可为空表示无序节点

    /**
     * 使用提供的znode名称实例化ZNodeName：并取其前缀名和顺序号
     *
     * @param name The name of the znode
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public ZNodeName(final String name) {
        this.name = Objects.requireNonNull(name, "ZNode name cannot be null");

        final int idx = name.lastIndexOf('-');
        // 处理节点的前缀名和顺序号
        if (idx < 0) { // 无顺序号
            this.prefix = name;
            this.sequence = Optional.empty();
        } else { // 有顺序号，并从name中截取
            this.prefix = name.substring(0, idx);
            this.sequence = Optional.ofNullable(parseSequenceString(name.substring(idx + 1)));
        }
    }

    /**
     * 处理String类型的顺序号
     * @param seq String类型的顺序号
     * @return Integer类型的顺序号
     */
    private Integer parseSequenceString(final String seq) {
        try {
            return Integer.parseInt(seq);
        } catch (Exception e) {
            LOG.warn("Number format exception for {}", seq, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "ZNodeName [name=" + name + ", prefix=" + prefix + ", sequence="
                + sequence + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZNodeName other = (ZNodeName) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * 根据ZNode的顺序号来做比较
     *
     * @param that 要比较的其他znode
     * @return 它们序列号之间的差异：如果这个znode的序列号较大，则为正值;如果它们的序列号相同，则为0;如果这个znode的序列号较低，则为负值
     */
    public int compareTo(final ZNodeName that) {
        // 如两个ZNode都存在顺序号
        if (this.sequence.isPresent() && that.sequence.isPresent()) {
            // 做比较
            int cseq = Integer.compare(this.sequence.get(), that.sequence.get());
            return (cseq != 0) ? cseq : this.prefix.compareTo(that.prefix);
        }
        // 当前存在，其他的that不存在顺序号。that的顺序号比较小，说明更早
        if (this.sequence.isPresent()) {
            return -1;
        }
        // 当前不存在，其他的that存在顺序号。that的顺序号比较大，说明较晚
        if (that.sequence.isPresent()) {
            return 1;
        }
        return this.prefix.compareTo(that.prefix);
    }

    /**
     * Returns the name of the znode.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the optional sequence number.
     */
    public Optional<Integer> getSequence() {
        return sequence;
    }

    /**
     * Returns the text prefix before the sequence number.
     */
    public String getPrefix() {
        return prefix;
    }

}
