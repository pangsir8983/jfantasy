package org.jfantasy.website.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.jfantasy.framework.dao.BaseBusEntity;

/**
 * pageItem 的子项
 */
@Entity
@Table(name = "SWP_PAGE_ITEM_DATA")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pageItem"})
public class PageItemData extends BaseBusEntity {

    @Id
    @Column(name = "ID", nullable = false, insertable = true, updatable = false, precision = 22, scale = 0)
    @GeneratedValue(generator = "fantasy-sequence")
    @GenericGenerator(name = "fantasy-sequence", strategy = "fantasy-sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAGE_ITEM_ID",foreignKey =  @ForeignKey(name = "FK_PAGE_ITEM_DATA"))
    private PageItem pageItem;

    @Column(name = "CLASS_NAME", length = 1000)
    private String className;

    @Column(name = "BEAN_ID")
    private String beanId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public PageItem getPageItem() {
        return pageItem;
    }

    public void setPageItem(PageItem pageItem) {
        this.pageItem = pageItem;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

}
