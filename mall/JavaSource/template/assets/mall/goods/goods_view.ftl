<#assign s=JspTaglibs["/WEB-INF/tlds/struts-tags.tld"]/>
<@s.set name="category" value="%{goods.category}"/>
<script type="text/javascript">
$(function(){
    var list = $('#goodsParameter').list($('#goodsParameterForm'),<@s.property value="@com.fantasy.framework.util.jackson.JSON@serialize(goods.customGoodsParameterValues)" escapeHtml="false" default="[]"/>);

});
</script>

<div class="pad10L pad10R">
<div class="example-box">
        <@s.form id="saveForm" namespace="/mall/goods" action="save" method="post" cssClass="center-margin">
        <div class="tabs ui-tabs ui-widget ui-widget-content ui-corner-all">
        <ul>
            <li>
                <a title="<@s.text name='mall.goods.base' />" href="#normal-tabs-1">
                    <@s.text name='mall.goods.base' />
                </a>
            </li>
            <li>
                <a title="<@s.text name='mall.goods.detail' />" href="#normal-tabs-2">
                    <@s.text name='mall.goods.detail' />
                </a>
            </li>
            <li>
                <a title="<@s.text name='mall.goods.paras' />" href="#normal-tabs-3">
                    <@s.text name='mall.goods.paras' />
                </a>
            </li>
            <li>
                <a title="<@s.text name='mall.goods.goodsImgs' />" href="#normal-tabs-4">
                    <@s.text name='mall.goods.goodsImgs' />
                </a>
            </li>
            <li>
                <a title="<@s.text name='mall.goods.searchGood' />" href="#normal-tabs-5">
                    <@s.text name='mall.goods.searchGood' />
                </a>
            </li>
        </ul>
        <a href="javascript:;" class="btn small hover-black float-right back-page" title="" style="margin-top: -30px;margin-right: 30px">
            <i class="glyph-icon icon-reply"></i>
        </a>
            <div id="normal-tabs-1">
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.title' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-left">
                                    <@s.property value="goods.name"/>
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.marketPrice' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-left">
                                    <@s.property value="goods.marketPrice"/>
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.price' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-left">
                                    <@s.property value="goods.price"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">

                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.english' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-right">
                                    <@s.property value="goods.engname"/>
                                </div>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.brand' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-right">
                                    <@s.property value="goods.brand.name"/>（<@s.property value="goods.brand.engname"/>）
                                </div>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-label col-md-3">
                                <label for="">
                                    <@s.text name='mall.goods.cost' />：
                                </label>
                            </div>
                            <div class="form-input col-md-9">
                                <div class="append-right">
                                    <@s.property value="goods.cost"/>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
            <div id="normal-tabs-2">
                <@s.textarea cssClass="ckeditor" name="introduction" cssStyle="width:900px;height:360px;" value="%{@com.fantasy.system.util.SettingUtil@toHtml(goods.introduction)}" readonly="true"/>
            </div>
            <div id="normal-tabs-3">
                <@s.if test="category.goodsParameters.size > 0">
                    <div class="row">
                        <@s.iterator value="category.goodsParameters" var="goodsParameter" status="st">
                            <@s.if test="#st.odd">
                                <div class="col-md-6">
                                    <div class="form-row">
                                        <div class="form-label col-md-2">
                                            <label for="">
                                                <@s.hidden value="%{#goodsParameter.id}">
                                                    <@s.param name="name">
                                                        goodsParameterValues[<@s.property value="#st.index"/>].id
                                                    </@s.param>
                                                </@s.hidden>
                                                <@s.property value="#goodsParameter.name"/>
                                            </label>
                                        </div>
                                        <div class="form-input col-md-10">
                                            <div class="append-left">
                                                <@s.textfield name="goodsParameterValues[%{#st.index}].value" value="%{@com.fantasy.framework.util.common.ObjectUtil@find(goods.goodsParameterValues,'id',#goodsParameter.id).value}" cssClass="w250" placeholder="%{#goodsParameter.format}"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </@s.if>
                            <@s.else>
                                <div class="col-md-6">
                                    <div class="form-row">
                                        <div class="form-label col-md-2">
                                            <label for="">
                                                <@s.hidden value="%{#goodsParameter.id}">
                                                    <@s.param name="name">
                                                        goodsParameterValues[<@s.property value="#st.index"/>].id
                                                    </@s.param>
                                                </@s.hidden>
                                                <@s.property value="#goodsParameter.name"/>
                                            </label>
                                        </div>
                                        <div class="form-input col-md-10">
                                            <div class="append-left">
                                                <@s.textfield name="goodsParameterValues[%{#st.index}].value" value="%{@com.fantasy.framework.util.common.ObjectUtil@find(goods.goodsParameterValues,'id',#goodsParameter.id).value}" cssClass="w250" placeholder="%{#goodsParameter.format}"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </@s.else>
                        </@s.iterator>
                    </div>
                </@s.if>
                <#include "include/goods_customGoodsParameter.ftl">
            </div>
            <div id="normal-tabs-4">
                <div id="goodsImage" style="padding-top:4px;">
                    <div id="goodsImageUploader"></div>
                </div>
            </div>
            <div id="normal-tabs-5">
                <div class="example-box">
                    <div class="example-code center-margin">
                            <div class="form-row">
                                <div class="form-label col-md-2">
                                    <label for="">
                                        <@s.text name='mall.goods.metaKeywords' />：
                                    </label>
                                </div>
                                <div class="form-input col-md-10">
                                    <@s.property value="goods.metaKeywords"/>
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-label col-md-2">
                                    <label for="">
                                        <@s.text name='mall.goods.metaDescription' />：
                                    </label>
                                </div>
                                <div class="form-input col-md-10">
                                    <@s.property value="goods.metaDescription"/>
                                </div>
                            </div>
                    </div>

                </div>
            </div>
        </div>
        </@s.form>

        <div class="form-row">
            <div class="form-input col-md-10">
                    <a href="javascript:void(0);" class="btn medium primary-bg radius-all-4 switch menu-view back-page "  title="<@s.text name='mall.goods.back' />" >
                            <span class="glyph-icon icon-separator">
                                  <i class="glyph-icon icon-reply"></i>
                            </span>
                             <span class="button-content">
                             <@s.text name='mall.goods.back' />
                             </span>
                    </a>
            </div>
        </div>
    </div>
</div>
