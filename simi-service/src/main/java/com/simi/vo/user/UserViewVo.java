package com.simi.vo.user;

import java.math.BigDecimal;

public class UserViewVo {
	
	private long userId;
	
	private String mobile;
	
	private String provinceName;
	
	private String name;
    
    private String sex;
    
    private	String headImg;

    private BigDecimal restMoney;

    private Integer score;
    
    private Integer exp;
    
    private Short userType;
    
    private String qrCode;
	    
    /**
     * 用户Im用户名
     */
    private String ImUsername;
    
    /**
     * 用户Im密码
     */
    private String ImPassword;
        
    /**
     * 绑定推送设备clientID
     */
    private String clientId;
    
    //是否属于某团队的员工
    private Short hasCompany;
    
    //所属团队的ID
    private Long companyId;
    
    private String companyName;
    
    //所属团队个数
    private int companyCount;
        
	public String getImUsername() {
		return ImUsername;
	}

	public void setImUsername(String imUsername) {
		ImUsername = imUsername;
	}

	public String getImPassword() {
		return ImPassword;
	}

	public void setImPassword(String imPassword) {
		ImPassword = imPassword;
	}

	public UserViewVo() {
		super();
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Short getHasCompany() {
		return hasCompany;
	}

	public void setHasCompany(Short hasCompany) {
		this.hasCompany = hasCompany;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public int getCompanyCount() {
		return companyCount;
	}

	public void setCompanyCount(int companyCount) {
		this.companyCount = companyCount;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getHeadImg() {
		return headImg;
	}

	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}

	public BigDecimal getRestMoney() {
		return restMoney;
	}

	public void setRestMoney(BigDecimal restMoney) {
		this.restMoney = restMoney;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getExp() {
		return exp;
	}

	public void setExp(Integer exp) {
		this.exp = exp;
	}

	public Short getUserType() {
		return userType;
	}

	public void setUserType(Short userType) {
		this.userType = userType;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

}
