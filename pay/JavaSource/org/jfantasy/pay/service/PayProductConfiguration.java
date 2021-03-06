package org.jfantasy.pay.service;

import org.jfantasy.framework.util.common.ObjectUtil;
import org.jfantasy.pay.product.*;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付配置
 */
public class PayProductConfiguration implements InitializingBean {

    /**
     * 所有支持的支付产品
     */
    private List<PayProduct> payProducts = new ArrayList<PayProduct>();

    public void setPayProducts(List<PayProduct> payProducts) {
        this.payProducts = payProducts;
    }

    public PayProduct loadPayProduct(String paymentProductId) {
        return ObjectUtil.find(this.payProducts, "id", paymentProductId);
    }

    // 获取所有支付产品集合
    public List<PayProduct> getPayProducts() {
        return this.payProducts;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //支付宝即时交易
        if (ObjectUtil.find(this.payProducts, "id", "alipayDirect") == null) {
            AlipayDirect alipayDirect = new AlipayDirect();
            alipayDirect.setId("alipayDirect");
            alipayDirect.setName("支付宝（即时交易）");
            alipayDirect.setBargainorIdName("合作身份者ID");
            alipayDirect.setBargainorKeyName("安全校验码");
            alipayDirect.setShroffAccountName("支付宝账号");
            alipayDirect.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            alipayDirect.setLogoPath("/template/tocer/images/payment/alipay_direct_icon.gif");
            alipayDirect.setDescription("支付宝即时交易，付款后立即到账，无预付/年费，单笔费率阶梯最低0.7%，无流量限制。 <a href=\"https://www.alipay.com/himalayas/practicality_customer.htm?customer_external_id=C4393933195131654818&market_type=from_agent_contract&pro_codes=61F99645EC0DC4380ADE569DD132AD7A\" target=\"_blank\"><span class=\"red\">立即申请</span></a>");
            this.payProducts.add(alipayDirect);
        }

        //支付宝WAP即时交易
        if (ObjectUtil.find(this.payProducts, "id", "alipayDirectByWap") == null) {
            AlipayDirectByWap alipayDirectByWap = new AlipayDirectByWap();
            alipayDirectByWap.setId("alipayDirectByWap");
            alipayDirectByWap.setName("支付宝（WAP即时交易）");
            alipayDirectByWap.setBargainorIdName("合作身份者ID");
            alipayDirectByWap.setBargainorKeyName("安全校验码");
            alipayDirectByWap.setShroffAccountName("支付宝账号");
            alipayDirectByWap.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            alipayDirectByWap.setLogoPath("/template/tocer/images/payment/alipay_direct_icon.gif");
            alipayDirectByWap.setDescription("支付宝即时交易，付款后立即到账，无预付/年费，单笔费率阶梯最低0.7%，无流量限制。 <a href=\"https://www.alipay.com/himalayas/practicality_customer.htm?customer_external_id=C4393933195131654818&market_type=from_agent_contract&pro_codes=61F99645EC0DC4380ADE569DD132AD7A\" target=\"_blank\"><span class=\"red\">立即申请</span></a>");
            this.payProducts.add(alipayDirectByWap);
        }

        //支付宝担保交易
        if (ObjectUtil.find(this.payProducts, "id", "alipayPartner") == null) {
            AlipayPartner alipayPartner = new AlipayPartner();
            alipayPartner.setId("alipayPartner");
            alipayPartner.setName("支付宝（担保交易）");
            alipayPartner.setBargainorIdName("合作身份者ID");
            alipayPartner.setBargainorKeyName("安全校验码");
            alipayPartner.setShroffAccountName("支付宝账号");
            alipayPartner.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            alipayPartner.setLogoPath("/template/tocer/images/payment/alipay_partner_icon.gif");
            alipayPartner.setDescription("支付宝担保交易，买家先付款到支付宝，支付宝收到买家付款后即时通知卖家发货，买家收到货物满意后通知支付宝付款给卖家。 <a href=\"https://www.alipay.com/himalayas/practicality_customer.htm?customer_external_id=C4393933195131654818&market_type=from_agent_contract&pro_codes=61F99645EC0DC4380ADE569DD132AD7A\" target=\"_blank\"><span class=\"red\">立即申请</span></a>");
            this.payProducts.add(alipayPartner);
        }

        //财付通即时交易
        if (ObjectUtil.find(this.payProducts, "id", "tenpayDirect") == null) {
            TenpayDirect tenpayDirect = new TenpayDirect();
            tenpayDirect.setId("tenpayDirect");
            tenpayDirect.setName("财付通（即时交易）");
            tenpayDirect.setBargainorIdName("商户号");
            tenpayDirect.setBargainorKeyName("安全校验码");
            tenpayDirect.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            tenpayDirect.setLogoPath("/template/tocer/images/payment/tenpay_direct_icon.gif");
            tenpayDirect.setDescription("中国领先的在线支付平台，致力于为互联网用户和企业提供安全、便捷、专业的在线支付服务。 <a href=\"http://union.tenpay.com/mch/mch_register.shtml?sp_suggestuser=admin@shopxx.net\" class=\"red\" target=\"_blank\"><span class=\"red\">立即申请</span></a>");
            this.payProducts.add(tenpayDirect);
        }

        //财付通（担保交易）
        if (ObjectUtil.find(this.payProducts, "id", "tenpayPartner") == null) {
            TenpayPartner tenpayPartner = new TenpayPartner();
            tenpayPartner.setId("tenpayPartner");
            tenpayPartner.setName("财付通（担保交易）");
            tenpayPartner.setBargainorIdName("商户号");
            tenpayPartner.setBargainorKeyName("安全校验码");
            tenpayPartner.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            tenpayPartner.setLogoPath("/template/tocer/images/payment/tenpay_partner_icon.gif");
            tenpayPartner.setDescription("中国领先的在线支付平台，致力于为互联网用户和企业提供安全、便捷、专业的在线支付服务。 <a href=\"http://union.tenpay.com/mch/mch_register.shtml?sp_suggestuser=admin@shopxx.net\" class=\"red\" target=\"_blank\"><span class=\"red\">立即申请</span></a>");
            this.payProducts.add(tenpayPartner);
        }

        //易宝支付
        if (ObjectUtil.find(this.payProducts, "id", "yeepay") == null) {
            Yeepay yeepay = new Yeepay();
            yeepay.setId("yeepay");
            yeepay.setName("易宝支付");
            yeepay.setBargainorIdName("商户编号");
            yeepay.setBargainorKeyName("密钥");
            yeepay.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            yeepay.setLogoPath("/template/tocer/images/payment/yeepay_icon.gif");
            yeepay.setDescription("中国领先的独立第三方支付平台，致力于为广大商家和消费者提供“安全、简单、快乐”的专业电子支付解决方案和服务。");
            this.payProducts.add(yeepay);
        }

        //快钱
        if (ObjectUtil.find(this.payProducts, "id", "pay99bill") == null) {
            Pay99BillSupport pay99bill = new Pay99BillSupport();
            pay99bill.setId("pay99bill");
            pay99bill.setName("快钱");
            pay99bill.setBargainorIdName("账户号");
            pay99bill.setBargainorKeyName("密钥");
            pay99bill.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            pay99bill.setLogoPath("/template/tocer/images/payment/pay99bill_icon.gif");
            pay99bill.setDescription("快钱是国内领先的独立第三方支付企业，旨在为各类企业及个人 提供安全、便捷和保密的综合电子支付服务。");
            this.payProducts.add(pay99bill);
        }

        //银联电子支付
        if (ObjectUtil.find(this.payProducts, "id", "chinapay") == null) {
            Chinapay chinapay = new Chinapay();
            chinapay.setId("chinapay");
            chinapay.setName("银联电子支付");
            chinapay.setBargainorIdName("商户号");
            chinapay.setBargainorKeyName("证书密码");
            chinapay.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            chinapay.setLogoPath("");
            chinapay.setDescription("银联电子支付服务有限公司（ChinaPay）是中国银联控股的银行卡专业化服务公司，拥有面向全国的统一支付平台，主要从事以互联网等新兴渠道为基础的网上支付、企业B2B账户支付、电话支付、网上跨行转账、网上基金交易、企业公对私资金代付、自助终端支付等银行卡网上支付及增值业务，是中国银联旗下的网络方面军。");
            this.payProducts.add(chinapay);
        }

        //银联支付
        if (ObjectUtil.find(this.payProducts, "id", "unionpay") == null) {
            Unionpay unionpay = new Unionpay();
            unionpay.setId("unionpay");
            unionpay.setName("银联支付");
            unionpay.setBargainorIdName("商户号");
            unionpay.setBargainorKeyName("证书密码");
            unionpay.setCurrencyTypes(new CurrencyType[]{CurrencyType.CNY});
            unionpay.setLogoPath("");
            unionpay.setDescription("");
            this.payProducts.add(unionpay);
        }


    }

    public void addPaymentProduct(PayProduct payProduct) {
        this.getPayProducts().add(payProduct);
    }

}
