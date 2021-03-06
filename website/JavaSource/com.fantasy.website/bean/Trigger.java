package org.jfantasy.website.bean;

import org.hibernate.annotations.GenericGenerator;
import org.jfantasy.framework.dao.BaseBusEntity;

/**
 * 触发器
 */
@Entity
@Table(name = "SWP_TRIGGER")
public class Trigger extends BaseBusEntity {

    public static enum Type {
        /**
         * 定时触发器
         * 参考 Spring Quartz
         */
        Scheduled,
        /**
         * 数据修改
         * 参考项：
         * 1.数据类型《classname》
         * 2.变更类型：新增、修改及删除
         * 3.变更范围：某个字段或者几个字段《 可以带条件如 attr > ?  & attr = ? 》
         * 4.监听范围:page/pageItem
         */
        EntityChanged
    }

    @Id
    @Column(name = "ID", nullable = false, insertable = true, updatable = false, precision = 22, scale = 0)
    @GeneratedValue(generator = "fantasy-sequence")
    @GenericGenerator(name = "fantasy-sequence", strategy = "fantasy-sequence")
    private Long id;
    /**
     * 触发类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "NAME")
    private Type type;
    /**
     * 触发规则存储
     */
    @Column(name = "VALUE")
    private String value;

    /**
     * 优先条件
     */
    @Column(name = "PRIORITYCONDITION")
    private String priorityCondition;
    /**
     * 触发器描述
     */
    private String description;

    /**
     * 对应的page
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="PAGE_ID",foreignKey = @ForeignKey(name="FK_PAGE_TRIGGER"))
    private Page page;

    /*
    Schedule editor
    1.Daily
    2.Days per week
    3.Days per month
    4.Cron expression
    */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getPriorityCondition() {
        return priorityCondition;
    }

    public void setPriorityCondition(String priorityCondition) {
        this.priorityCondition = priorityCondition;
    }
}
