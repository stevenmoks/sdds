package com.risen.entity;

import java.util.Date;

/**
 * RisenDevparty entity.
 * 
 * @author MyEclipse Persistence Tools
 */

public class RisenDevparty implements java.io.Serializable {

	// Fields

	private Integer id;
	private Date risendpCdate;
	private Date risendpUdate;
	private String risendpName;
	private String risendpIdnumber;
	private String risendpSex;
	private String risendpBirth;
	private String risendpBranch;
	private String risendpNation;
	private String risendpNative;
	private String risendpMarriage;
	private String risendpSdate;
	private String risendpInfo;
	private Date risendpTalktime;
	private String risendpTalkperson;
	private String risendpTalkinfo;
	private Date risendpActivetime;
	private String risendpActiveinfo;
	private Date risendpRegtime;
	private Date risendpNoticetime;
	private Date risendpConvoketime;
	private Date risendpOnfiletime;
	private String risendpLinkman;
	private String risendpRecinfo;
	private String risendpOrgrecinfo;
	private String risendpOnfileinfo;
	private String risendpFiveBranch;
	private String risendpFiveName;
	private String risendpFiveSex;
	private String risendpFiveBirth;
	private String risendpFiveEducation;
	private String risendpFiveNation;
	private Date risendpFiveInpartytime;
	private String risendpFiveRelation;
	private Date risendpSixTime;
	private String risendpSixPerson;
	private String risendpSixInfo;
	private String risendpSixRelatedinfo;
	private Date risendpSevenTime;
	private String risendpSevenName;
	private String risendpSevenOpinion;
	private String risendpSevenInfo;
	private Date risendpEightTime;
	private Date risendpEightDiscusstime;
	private Date risendpEightActivetime;
	private String risendpNineBranch;
	private String risendpNineName;
	private String risendpNineXl;
	private String risendpNineSex;
	private String risendpNineBirth;
	private String risendpNineNation;
	private Date risendpNineInpartytime;
	private String risendpNineRelation;
	private String risendpTenInfo;
	private Date risendpElevenTime;
	private String risendpElevenScore;
	private String risendpElevenAddress;
	private String risendpElevenContent;
	private String risendpElevenInfo;
	private Date risendpTwelveTime;
	private String risendpTwelvePer;
	private String risendpTwelveOpinion;
	private String risendpTwelveInfo;
	private Date risendpThirteenTime;
	private String risendpThirteenPer;
	private String risendpThirteenOpinion;
	private String risendpFourteenNo;
	private Date risendpFifteenTime;
	private String risendpFifteenCount;
	private String risendpFifteenShould;
	private String risendpFifteenActual;
	private String risendpFifteenPoweractual;
	private String risendpFifteenConsent;
	private String risendpFifteenDisagree;
	private String risendpFifteenDefault;
	private String risendpFifteenInfo;
	private Date risendpSixteenTime;
	private String risendpSixteenPerson;
	private String risendpSixteenStatus;
	private String risendpSixteenInfo;
	private Date risendpSeventeenTime;
	private String risendpSeventeenPerson;
	private String risendpSeventeenOpinion;
	private String risendpSeventeenInfo;
	private String risendpEighteenDept;
	private String risendpEighteenPost;
	private String risendpEighteenNo;
	private Date risendpEighteenTime;
	private Date risendpEighteenActivetime;
	private Date risendpEighteenInpartytime;
	private Date risendpEighteenObjtime;
	private String risendpEighteenNoticestatus;
	private String risendpEighteenInfo;
	private Date risendpNineteenTime;
	private String risendpNineteenName;
	private String risendpNineteenInfo;
	private Date risendpTwentyTime;
	private String risendpTwentyInfo;
	private Date risendpTwentyoneTime;
	private String risendpTwentyonePer;
	private String risendpTwentyoneStatus;
	private String risendpTwentyoneInfo;
	private Date risendpTwentytwoTime;
	private String risendpTwentytwoInfo;
	private Date risendpTwentythreeTime;
	private String risendpTwentythreeCount;
	private String risendpTwentythreeShoult;
	private String risendpTwentythreePower;
	private String risendpTwentythreeActual;
	private String risendpTwentythreeConsent;
	private String risendpTwentythreeDisagree;
	private String risendpTwentythreeDefault;
	private String risendpTwentythreeName;
	private Date risendpTwentythreeMarktime;
	private Date risendpTwentythreeLengthen;
	private String risendpTwentythreeInfo;
	private Date risendpTwentyfourTime;
	private Date risendpTwentyfourBranch;
	private String risendpTwentyfourPerson;
	private String risendpTwentyfourMarktime;
	private String risendpTwentyfourOpinion;
	private String risendpTwentyfourInfo;
	private String risendpTwentyfiveAddress;
	private String risendpTwentyfiveInfo;
	//流程标记
	private String risendpExpands1;
	//
	private String risendpExpands2;
	//党组织ID
	private String risendpExpands3;
	//对应Cmsuser ID，最后一步将其更新为在职党员
	private String risendpExpands4;
	private String risendpExpands5;
	private String risendpExpands6;
	private String updateMark;
	private String risendpExpands7;
	
	// Constructors

	public String getUpdateMark() {
		return updateMark;
	}

	public String getRisendpExpands7() {
		return risendpExpands7;
	}

	public void setRisendpExpands7(String risendpExpands7) {
		this.risendpExpands7 = risendpExpands7;
	}

	public void setUpdateMark(String updateMark) {
		this.updateMark = updateMark;
	}

	/** full constructor *//*
	public RisenDevparty(Integer id, Date risendpCdate, Date risendpUdate,
			String risendpName, String risendpIdnumber, String risendpSex,
			String risendpBirth, String risendpBranch, String risendpNation,
			String risendpNative, String risendpMarriage, String risendpSdate,
			String risendpInfo, Date risendpTalktime, Date risendpTalkperson,
			String risendpTalkinfo, Date risendpActivetime,
			String risendpActiveinfo, Date risendpRegtime,
			Date risendpNoticetime, Date risendpConvoketime,
			Date risendpOnfiletime, String risendpLinkman,
			String risendpRecinfo, String risendpOrgrecinfo,
			String risendpOnfileinfo, String risendpFiveBranch,
			String risendpFiveName, String risendpFiveSex,
			String risendpFiveBirth, String risendpFiveEducation,
			String risendpFiveNation, Date risendpFiveInpartytime,
			String risendpFiveRelation, Date risendpSixTime,
			String risendpSixPerson, String risendpSixInfo,
			String risendpSixRelatedinfo, Date risendpSevenTime,
			String risendpSevenName, String risendpSevenOpinion,
			String risendpSevenInfo, Date risendpEightTime,
			Date risendpEightDiscusstime, Date risendpEightActivetime,
			String risendpNineBranch, String risendpNineName,
			String risendpNineXl, String risendpNineSex,
			String risendpNineBirth, String risendpNineNation,
			Date risendpNineInpartytime, String risendpNineRelation,
			String risendpTenInfo, Date risendpElevenTime,
			String risendpElevenScore, String risendpElevenAddress,
			String risendpElevenContent, String risendpElevenInfo,
			Date risendpTwelveTime, String risendpTwelvePer,
			String risendpTwelveOpinion, String risendpTwelveInfo,
			Date risendpThirteenTime, String risendpThirteenPer,
			String risendpThirteenOpinion, String risendpFourteenNo,
			Date risendpFifteenTime, String risendpFifteenCount,
			String risendpFifteenShould, String risendpFifteenActual,
			String risendpFifteenPoweractual, String risendpFifteenConsent,
			String risendpFifteenDisagree, String risendpFifteenDefault,
			String risendpFifteenInfo, Date risendpSixteenTime,
			String risendpSixteenPerson, String risendpSixteenStatus,
			String risendpSixteenInfo, Date risendpSeventeenTime,
			String risendpSeventeenPerson, String risendpSeventeenOpinion,
			String risendpSeventeenInfo, String risendpEighteenDept,
			String risendpEighteenPost, String risendpEighteenNo,
			Date risendpEighteenTime, Date risendpEighteenActivetime,
			Date risendpEighteenInpartytime, Date risendpEighteenObjtime,
			String risendpEighteenNoticestatus, String risendpEighteenInfo,
			Date risendpNineteenTime, String risendpNineteenName,
			String risendpNineteenInfo, Date risendpTwentyTime,
			String risendpTwentyInfo, Date risendpTwentyoneTime,
			String risendpTwentyonePer, String risendpTwentyoneStatus,
			String risendpTwentyoneInfo, Date risendpTwentytwoTime,
			String risendpTwentytwoInfo, Date risendpTwentythreeTime,
			String risendpTwentythreeCount, String risendpTwentythreeShoult,
			String risendpTwentythreePower, String risendpTwentythreeActual,
			String risendpTwentythreeConsent,
			String risendpTwentythreeDisagree,
			String risendpTwentythreeDefault, String risendpTwentythreeName,
			Date risendpTwentythreeMarktime, Date risendpTwentythreeLengthen,
			String risendpTwentythreeInfo, Date risendpTwentyfourTime,
			Date risendpTwentyfourBranch, String risendpTwentyfourPerson,
			String risendpTwentyfourMarktime, String risendpTwentyfourOpinion,
			String risendpTwentyfourInfo, String risendpTwentyfiveAddress,
			String risendpTwentyfiveInfo, String risendpExpands1,
			String risendpExpands2, String risendpExpands3,
			String risendpExpands4, String risendpExpands5,
			String risendpExpands6) {
		this.id = id;
		this.risendpCdate = risendpCdate;
		this.risendpUdate = risendpUdate;
		this.risendpName = risendpName;
		this.risendpIdnumber = risendpIdnumber;
		this.risendpSex = risendpSex;
		this.risendpBirth = risendpBirth;
		this.risendpBranch = risendpBranch;
		this.risendpNation = risendpNation;
		this.risendpNative = risendpNative;
		this.risendpMarriage = risendpMarriage;
		this.risendpSdate = risendpSdate;
		this.risendpInfo = risendpInfo;
		this.risendpTalktime = risendpTalktime;
		this.risendpTalkperson = risendpTalkperson;
		this.risendpTalkinfo = risendpTalkinfo;
		this.risendpActivetime = risendpActivetime;
		this.risendpActiveinfo = risendpActiveinfo;
		this.risendpRegtime = risendpRegtime;
		this.risendpNoticetime = risendpNoticetime;
		this.risendpConvoketime = risendpConvoketime;
		this.risendpOnfiletime = risendpOnfiletime;
		this.risendpLinkman = risendpLinkman;
		this.risendpRecinfo = risendpRecinfo;
		this.risendpOrgrecinfo = risendpOrgrecinfo;
		this.risendpOnfileinfo = risendpOnfileinfo;
		this.risendpFiveBranch = risendpFiveBranch;
		this.risendpFiveName = risendpFiveName;
		this.risendpFiveSex = risendpFiveSex;
		this.risendpFiveBirth = risendpFiveBirth;
		this.risendpFiveEducation = risendpFiveEducation;
		this.risendpFiveNation = risendpFiveNation;
		this.risendpFiveInpartytime = risendpFiveInpartytime;
		this.risendpFiveRelation = risendpFiveRelation;
		this.risendpSixTime = risendpSixTime;
		this.risendpSixPerson = risendpSixPerson;
		this.risendpSixInfo = risendpSixInfo;
		this.risendpSixRelatedinfo = risendpSixRelatedinfo;
		this.risendpSevenTime = risendpSevenTime;
		this.risendpSevenName = risendpSevenName;
		this.risendpSevenOpinion = risendpSevenOpinion;
		this.risendpSevenInfo = risendpSevenInfo;
		this.risendpEightTime = risendpEightTime;
		this.risendpEightDiscusstime = risendpEightDiscusstime;
		this.risendpEightActivetime = risendpEightActivetime;
		this.risendpNineBranch = risendpNineBranch;
		this.risendpNineName = risendpNineName;
		this.risendpNineXl = risendpNineXl;
		this.risendpNineSex = risendpNineSex;
		this.risendpNineBirth = risendpNineBirth;
		this.risendpNineNation = risendpNineNation;
		this.risendpNineInpartytime = risendpNineInpartytime;
		this.risendpNineRelation = risendpNineRelation;
		this.risendpTenInfo = risendpTenInfo;
		this.risendpElevenTime = risendpElevenTime;
		this.risendpElevenScore = risendpElevenScore;
		this.risendpElevenAddress = risendpElevenAddress;
		this.risendpElevenContent = risendpElevenContent;
		this.risendpElevenInfo = risendpElevenInfo;
		this.risendpTwelveTime = risendpTwelveTime;
		this.risendpTwelvePer = risendpTwelvePer;
		this.risendpTwelveOpinion = risendpTwelveOpinion;
		this.risendpTwelveInfo = risendpTwelveInfo;
		this.risendpThirteenTime = risendpThirteenTime;
		this.risendpThirteenPer = risendpThirteenPer;
		this.risendpThirteenOpinion = risendpThirteenOpinion;
		this.risendpFourteenNo = risendpFourteenNo;
		this.risendpFifteenTime = risendpFifteenTime;
		this.risendpFifteenCount = risendpFifteenCount;
		this.risendpFifteenShould = risendpFifteenShould;
		this.risendpFifteenActual = risendpFifteenActual;
		this.risendpFifteenPoweractual = risendpFifteenPoweractual;
		this.risendpFifteenConsent = risendpFifteenConsent;
		this.risendpFifteenDisagree = risendpFifteenDisagree;
		this.risendpFifteenDefault = risendpFifteenDefault;
		this.risendpFifteenInfo = risendpFifteenInfo;
		this.risendpSixteenTime = risendpSixteenTime;
		this.risendpSixteenPerson = risendpSixteenPerson;
		this.risendpSixteenStatus = risendpSixteenStatus;
		this.risendpSixteenInfo = risendpSixteenInfo;
		this.risendpSeventeenTime = risendpSeventeenTime;
		this.risendpSeventeenPerson = risendpSeventeenPerson;
		this.risendpSeventeenOpinion = risendpSeventeenOpinion;
		this.risendpSeventeenInfo = risendpSeventeenInfo;
		this.risendpEighteenDept = risendpEighteenDept;
		this.risendpEighteenPost = risendpEighteenPost;
		this.risendpEighteenNo = risendpEighteenNo;
		this.risendpEighteenTime = risendpEighteenTime;
		this.risendpEighteenActivetime = risendpEighteenActivetime;
		this.risendpEighteenInpartytime = risendpEighteenInpartytime;
		this.risendpEighteenObjtime = risendpEighteenObjtime;
		this.risendpEighteenNoticestatus = risendpEighteenNoticestatus;
		this.risendpEighteenInfo = risendpEighteenInfo;
		this.risendpNineteenTime = risendpNineteenTime;
		this.risendpNineteenName = risendpNineteenName;
		this.risendpNineteenInfo = risendpNineteenInfo;
		this.risendpTwentyTime = risendpTwentyTime;
		this.risendpTwentyInfo = risendpTwentyInfo;
		this.risendpTwentyoneTime = risendpTwentyoneTime;
		this.risendpTwentyonePer = risendpTwentyonePer;
		this.risendpTwentyoneStatus = risendpTwentyoneStatus;
		this.risendpTwentyoneInfo = risendpTwentyoneInfo;
		this.risendpTwentytwoTime = risendpTwentytwoTime;
		this.risendpTwentytwoInfo = risendpTwentytwoInfo;
		this.risendpTwentythreeTime = risendpTwentythreeTime;
		this.risendpTwentythreeCount = risendpTwentythreeCount;
		this.risendpTwentythreeShoult = risendpTwentythreeShoult;
		this.risendpTwentythreePower = risendpTwentythreePower;
		this.risendpTwentythreeActual = risendpTwentythreeActual;
		this.risendpTwentythreeConsent = risendpTwentythreeConsent;
		this.risendpTwentythreeDisagree = risendpTwentythreeDisagree;
		this.risendpTwentythreeDefault = risendpTwentythreeDefault;
		this.risendpTwentythreeName = risendpTwentythreeName;
		this.risendpTwentythreeMarktime = risendpTwentythreeMarktime;
		this.risendpTwentythreeLengthen = risendpTwentythreeLengthen;
		this.risendpTwentythreeInfo = risendpTwentythreeInfo;
		this.risendpTwentyfourTime = risendpTwentyfourTime;
		this.risendpTwentyfourBranch = risendpTwentyfourBranch;
		this.risendpTwentyfourPerson = risendpTwentyfourPerson;
		this.risendpTwentyfourMarktime = risendpTwentyfourMarktime;
		this.risendpTwentyfourOpinion = risendpTwentyfourOpinion;
		this.risendpTwentyfourInfo = risendpTwentyfourInfo;
		this.risendpTwentyfiveAddress = risendpTwentyfiveAddress;
		this.risendpTwentyfiveInfo = risendpTwentyfiveInfo;
		this.risendpExpands1 = risendpExpands1;
		this.risendpExpands2 = risendpExpands2;
		this.risendpExpands3 = risendpExpands3;
		this.risendpExpands4 = risendpExpands4;
		this.risendpExpands5 = risendpExpands5;
		this.risendpExpands6 = risendpExpands6;
	}*/

	// Property accessors

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getRisendpCdate() {
		return this.risendpCdate;
	}

	public void setRisendpCdate(Date risendpCdate) {
		this.risendpCdate = risendpCdate;
	}

	public Date getRisendpUdate() {
		return this.risendpUdate;
	}

	public void setRisendpUdate(Date risendpUdate) {
		this.risendpUdate = risendpUdate;
	}

	public String getRisendpName() {
		return this.risendpName;
	}

	public void setRisendpName(String risendpName) {
		this.risendpName = risendpName;
	}

	public String getRisendpIdnumber() {
		return this.risendpIdnumber;
	}

	public void setRisendpIdnumber(String risendpIdnumber) {
		this.risendpIdnumber = risendpIdnumber;
	}

	public String getRisendpSex() {
		return this.risendpSex;
	}

	public void setRisendpSex(String risendpSex) {
		this.risendpSex = risendpSex;
	}

	public String getRisendpBirth() {
		return this.risendpBirth;
	}

	public void setRisendpBirth(String risendpBirth) {
		this.risendpBirth = risendpBirth;
	}

	public String getRisendpBranch() {
		return this.risendpBranch;
	}

	public void setRisendpBranch(String risendpBranch) {
		this.risendpBranch = risendpBranch;
	}

	public String getRisendpNation() {
		return this.risendpNation;
	}

	public void setRisendpNation(String risendpNation) {
		this.risendpNation = risendpNation;
	}

	public String getRisendpNative() {
		return this.risendpNative;
	}

	public void setRisendpNative(String risendpNative) {
		this.risendpNative = risendpNative;
	}

	public String getRisendpMarriage() {
		return this.risendpMarriage;
	}

	public void setRisendpMarriage(String risendpMarriage) {
		this.risendpMarriage = risendpMarriage;
	}

	public String getRisendpSdate() {
		return this.risendpSdate;
	}

	public void setRisendpSdate(String risendpSdate) {
		this.risendpSdate = risendpSdate;
	}

	public String getRisendpInfo() {
		return this.risendpInfo;
	}

	public void setRisendpInfo(String risendpInfo) {
		this.risendpInfo = risendpInfo;
	}

	public Date getRisendpTalktime() {
		return this.risendpTalktime;
	}

	public void setRisendpTalktime(Date risendpTalktime) {
		this.risendpTalktime = risendpTalktime;
	}

	public String getRisendpTalkperson() {
		return this.risendpTalkperson;
	}

	public void setRisendpTalkperson(String risendpTalkperson) {
		this.risendpTalkperson = risendpTalkperson;
	}

	public String getRisendpTalkinfo() {
		return this.risendpTalkinfo;
	}

	public void setRisendpTalkinfo(String risendpTalkinfo) {
		this.risendpTalkinfo = risendpTalkinfo;
	}

	public Date getRisendpActivetime() {
		return this.risendpActivetime;
	}

	public void setRisendpActivetime(Date risendpActivetime) {
		this.risendpActivetime = risendpActivetime;
	}

	public String getRisendpActiveinfo() {
		return this.risendpActiveinfo;
	}

	public void setRisendpActiveinfo(String risendpActiveinfo) {
		this.risendpActiveinfo = risendpActiveinfo;
	}

	public Date getRisendpRegtime() {
		return this.risendpRegtime;
	}

	public void setRisendpRegtime(Date risendpRegtime) {
		this.risendpRegtime = risendpRegtime;
	}

	public Date getRisendpNoticetime() {
		return this.risendpNoticetime;
	}

	public void setRisendpNoticetime(Date risendpNoticetime) {
		this.risendpNoticetime = risendpNoticetime;
	}

	public Date getRisendpConvoketime() {
		return this.risendpConvoketime;
	}

	public void setRisendpConvoketime(Date risendpConvoketime) {
		this.risendpConvoketime = risendpConvoketime;
	}

	public Date getRisendpOnfiletime() {
		return this.risendpOnfiletime;
	}

	public void setRisendpOnfiletime(Date risendpOnfiletime) {
		this.risendpOnfiletime = risendpOnfiletime;
	}

	public String getRisendpLinkman() {
		return this.risendpLinkman;
	}

	public void setRisendpLinkman(String risendpLinkman) {
		this.risendpLinkman = risendpLinkman;
	}

	public String getRisendpRecinfo() {
		return this.risendpRecinfo;
	}

	public void setRisendpRecinfo(String risendpRecinfo) {
		this.risendpRecinfo = risendpRecinfo;
	}

	public String getRisendpOrgrecinfo() {
		return this.risendpOrgrecinfo;
	}

	public void setRisendpOrgrecinfo(String risendpOrgrecinfo) {
		this.risendpOrgrecinfo = risendpOrgrecinfo;
	}

	public String getRisendpOnfileinfo() {
		return this.risendpOnfileinfo;
	}

	public void setRisendpOnfileinfo(String risendpOnfileinfo) {
		this.risendpOnfileinfo = risendpOnfileinfo;
	}

	public String getRisendpFiveBranch() {
		return this.risendpFiveBranch;
	}

	public void setRisendpFiveBranch(String risendpFiveBranch) {
		this.risendpFiveBranch = risendpFiveBranch;
	}

	public String getRisendpFiveName() {
		return this.risendpFiveName;
	}

	public void setRisendpFiveName(String risendpFiveName) {
		this.risendpFiveName = risendpFiveName;
	}

	public String getRisendpFiveSex() {
		return this.risendpFiveSex;
	}

	public void setRisendpFiveSex(String risendpFiveSex) {
		this.risendpFiveSex = risendpFiveSex;
	}

	public String getRisendpFiveBirth() {
		return this.risendpFiveBirth;
	}

	public void setRisendpFiveBirth(String risendpFiveBirth) {
		this.risendpFiveBirth = risendpFiveBirth;
	}

	public String getRisendpFiveEducation() {
		return this.risendpFiveEducation;
	}

	public void setRisendpFiveEducation(String risendpFiveEducation) {
		this.risendpFiveEducation = risendpFiveEducation;
	}

	public String getRisendpFiveNation() {
		return this.risendpFiveNation;
	}

	public void setRisendpFiveNation(String risendpFiveNation) {
		this.risendpFiveNation = risendpFiveNation;
	}

	public Date getRisendpFiveInpartytime() {
		return this.risendpFiveInpartytime;
	}

	public void setRisendpFiveInpartytime(Date risendpFiveInpartytime) {
		this.risendpFiveInpartytime = risendpFiveInpartytime;
	}

	public String getRisendpFiveRelation() {
		return this.risendpFiveRelation;
	}

	public void setRisendpFiveRelation(String risendpFiveRelation) {
		this.risendpFiveRelation = risendpFiveRelation;
	}

	public Date getRisendpSixTime() {
		return this.risendpSixTime;
	}

	public void setRisendpSixTime(Date risendpSixTime) {
		this.risendpSixTime = risendpSixTime;
	}

	public String getRisendpSixPerson() {
		return this.risendpSixPerson;
	}

	public void setRisendpSixPerson(String risendpSixPerson) {
		this.risendpSixPerson = risendpSixPerson;
	}

	public String getRisendpSixInfo() {
		return this.risendpSixInfo;
	}

	public void setRisendpSixInfo(String risendpSixInfo) {
		this.risendpSixInfo = risendpSixInfo;
	}

	public String getRisendpSixRelatedinfo() {
		return this.risendpSixRelatedinfo;
	}

	public void setRisendpSixRelatedinfo(String risendpSixRelatedinfo) {
		this.risendpSixRelatedinfo = risendpSixRelatedinfo;
	}

	public Date getRisendpSevenTime() {
		return this.risendpSevenTime;
	}

	public void setRisendpSevenTime(Date risendpSevenTime) {
		this.risendpSevenTime = risendpSevenTime;
	}

	public String getRisendpSevenName() {
		return this.risendpSevenName;
	}

	public void setRisendpSevenName(String risendpSevenName) {
		this.risendpSevenName = risendpSevenName;
	}

	public String getRisendpSevenOpinion() {
		return this.risendpSevenOpinion;
	}

	public void setRisendpSevenOpinion(String risendpSevenOpinion) {
		this.risendpSevenOpinion = risendpSevenOpinion;
	}

	public String getRisendpSevenInfo() {
		return this.risendpSevenInfo;
	}

	public void setRisendpSevenInfo(String risendpSevenInfo) {
		this.risendpSevenInfo = risendpSevenInfo;
	}

	public Date getRisendpEightTime() {
		return this.risendpEightTime;
	}

	public void setRisendpEightTime(Date risendpEightTime) {
		this.risendpEightTime = risendpEightTime;
	}

	public Date getRisendpEightDiscusstime() {
		return this.risendpEightDiscusstime;
	}

	public void setRisendpEightDiscusstime(Date risendpEightDiscusstime) {
		this.risendpEightDiscusstime = risendpEightDiscusstime;
	}

	public Date getRisendpEightActivetime() {
		return this.risendpEightActivetime;
	}

	public void setRisendpEightActivetime(Date risendpEightActivetime) {
		this.risendpEightActivetime = risendpEightActivetime;
	}

	public String getRisendpNineBranch() {
		return this.risendpNineBranch;
	}

	public void setRisendpNineBranch(String risendpNineBranch) {
		this.risendpNineBranch = risendpNineBranch;
	}

	public String getRisendpNineName() {
		return this.risendpNineName;
	}

	public void setRisendpNineName(String risendpNineName) {
		this.risendpNineName = risendpNineName;
	}

	public String getRisendpNineXl() {
		return this.risendpNineXl;
	}

	public void setRisendpNineXl(String risendpNineXl) {
		this.risendpNineXl = risendpNineXl;
	}

	public String getRisendpNineSex() {
		return this.risendpNineSex;
	}

	public void setRisendpNineSex(String risendpNineSex) {
		this.risendpNineSex = risendpNineSex;
	}

	public String getRisendpNineBirth() {
		return this.risendpNineBirth;
	}

	public void setRisendpNineBirth(String risendpNineBirth) {
		this.risendpNineBirth = risendpNineBirth;
	}

	public String getRisendpNineNation() {
		return this.risendpNineNation;
	}

	public void setRisendpNineNation(String risendpNineNation) {
		this.risendpNineNation = risendpNineNation;
	}

	public Date getRisendpNineInpartytime() {
		return this.risendpNineInpartytime;
	}

	public void setRisendpNineInpartytime(Date risendpNineInpartytime) {
		this.risendpNineInpartytime = risendpNineInpartytime;
	}

	public String getRisendpNineRelation() {
		return this.risendpNineRelation;
	}

	public void setRisendpNineRelation(String risendpNineRelation) {
		this.risendpNineRelation = risendpNineRelation;
	}

	public String getRisendpTenInfo() {
		return this.risendpTenInfo;
	}

	public void setRisendpTenInfo(String risendpTenInfo) {
		this.risendpTenInfo = risendpTenInfo;
	}

	public Date getRisendpElevenTime() {
		return this.risendpElevenTime;
	}

	public void setRisendpElevenTime(Date risendpElevenTime) {
		this.risendpElevenTime = risendpElevenTime;
	}

	public String getRisendpElevenScore() {
		return this.risendpElevenScore;
	}

	public void setRisendpElevenScore(String risendpElevenScore) {
		this.risendpElevenScore = risendpElevenScore;
	}

	public String getRisendpElevenAddress() {
		return this.risendpElevenAddress;
	}

	public void setRisendpElevenAddress(String risendpElevenAddress) {
		this.risendpElevenAddress = risendpElevenAddress;
	}

	public String getRisendpElevenContent() {
		return this.risendpElevenContent;
	}

	public void setRisendpElevenContent(String risendpElevenContent) {
		this.risendpElevenContent = risendpElevenContent;
	}

	public String getRisendpElevenInfo() {
		return this.risendpElevenInfo;
	}

	public void setRisendpElevenInfo(String risendpElevenInfo) {
		this.risendpElevenInfo = risendpElevenInfo;
	}

	public Date getRisendpTwelveTime() {
		return this.risendpTwelveTime;
	}

	public void setRisendpTwelveTime(Date risendpTwelveTime) {
		this.risendpTwelveTime = risendpTwelveTime;
	}

	public String getRisendpTwelvePer() {
		return this.risendpTwelvePer;
	}

	public void setRisendpTwelvePer(String risendpTwelvePer) {
		this.risendpTwelvePer = risendpTwelvePer;
	}

	public String getRisendpTwelveOpinion() {
		return this.risendpTwelveOpinion;
	}

	public void setRisendpTwelveOpinion(String risendpTwelveOpinion) {
		this.risendpTwelveOpinion = risendpTwelveOpinion;
	}

	public String getRisendpTwelveInfo() {
		return this.risendpTwelveInfo;
	}

	public void setRisendpTwelveInfo(String risendpTwelveInfo) {
		this.risendpTwelveInfo = risendpTwelveInfo;
	}

	public Date getRisendpThirteenTime() {
		return this.risendpThirteenTime;
	}

	public void setRisendpThirteenTime(Date risendpThirteenTime) {
		this.risendpThirteenTime = risendpThirteenTime;
	}

	public String getRisendpThirteenPer() {
		return this.risendpThirteenPer;
	}

	public void setRisendpThirteenPer(String risendpThirteenPer) {
		this.risendpThirteenPer = risendpThirteenPer;
	}

	public String getRisendpThirteenOpinion() {
		return this.risendpThirteenOpinion;
	}

	public void setRisendpThirteenOpinion(String risendpThirteenOpinion) {
		this.risendpThirteenOpinion = risendpThirteenOpinion;
	}

	public String getRisendpFourteenNo() {
		return this.risendpFourteenNo;
	}

	public void setRisendpFourteenNo(String risendpFourteenNo) {
		this.risendpFourteenNo = risendpFourteenNo;
	}

	public Date getRisendpFifteenTime() {
		return this.risendpFifteenTime;
	}

	public void setRisendpFifteenTime(Date risendpFifteenTime) {
		this.risendpFifteenTime = risendpFifteenTime;
	}

	public String getRisendpFifteenCount() {
		return this.risendpFifteenCount;
	}

	public void setRisendpFifteenCount(String risendpFifteenCount) {
		this.risendpFifteenCount = risendpFifteenCount;
	}

	public String getRisendpFifteenShould() {
		return this.risendpFifteenShould;
	}

	public void setRisendpFifteenShould(String risendpFifteenShould) {
		this.risendpFifteenShould = risendpFifteenShould;
	}

	public String getRisendpFifteenActual() {
		return this.risendpFifteenActual;
	}

	public void setRisendpFifteenActual(String risendpFifteenActual) {
		this.risendpFifteenActual = risendpFifteenActual;
	}

	public String getRisendpFifteenPoweractual() {
		return this.risendpFifteenPoweractual;
	}

	public void setRisendpFifteenPoweractual(String risendpFifteenPoweractual) {
		this.risendpFifteenPoweractual = risendpFifteenPoweractual;
	}

	public String getRisendpFifteenConsent() {
		return this.risendpFifteenConsent;
	}

	public void setRisendpFifteenConsent(String risendpFifteenConsent) {
		this.risendpFifteenConsent = risendpFifteenConsent;
	}

	public String getRisendpFifteenDisagree() {
		return this.risendpFifteenDisagree;
	}

	public void setRisendpFifteenDisagree(String risendpFifteenDisagree) {
		this.risendpFifteenDisagree = risendpFifteenDisagree;
	}

	public String getRisendpFifteenDefault() {
		return this.risendpFifteenDefault;
	}

	public void setRisendpFifteenDefault(String risendpFifteenDefault) {
		this.risendpFifteenDefault = risendpFifteenDefault;
	}

	public String getRisendpFifteenInfo() {
		return this.risendpFifteenInfo;
	}

	public void setRisendpFifteenInfo(String risendpFifteenInfo) {
		this.risendpFifteenInfo = risendpFifteenInfo;
	}

	public Date getRisendpSixteenTime() {
		return this.risendpSixteenTime;
	}

	public void setRisendpSixteenTime(Date risendpSixteenTime) {
		this.risendpSixteenTime = risendpSixteenTime;
	}

	public String getRisendpSixteenPerson() {
		return this.risendpSixteenPerson;
	}

	public void setRisendpSixteenPerson(String risendpSixteenPerson) {
		this.risendpSixteenPerson = risendpSixteenPerson;
	}

	public String getRisendpSixteenStatus() {
		return this.risendpSixteenStatus;
	}

	public void setRisendpSixteenStatus(String risendpSixteenStatus) {
		this.risendpSixteenStatus = risendpSixteenStatus;
	}

	public String getRisendpSixteenInfo() {
		return this.risendpSixteenInfo;
	}

	public void setRisendpSixteenInfo(String risendpSixteenInfo) {
		this.risendpSixteenInfo = risendpSixteenInfo;
	}

	public Date getRisendpSeventeenTime() {
		return this.risendpSeventeenTime;
	}

	public void setRisendpSeventeenTime(Date risendpSeventeenTime) {
		this.risendpSeventeenTime = risendpSeventeenTime;
	}

	public String getRisendpSeventeenPerson() {
		return this.risendpSeventeenPerson;
	}

	public void setRisendpSeventeenPerson(String risendpSeventeenPerson) {
		this.risendpSeventeenPerson = risendpSeventeenPerson;
	}

	public String getRisendpSeventeenOpinion() {
		return this.risendpSeventeenOpinion;
	}

	public void setRisendpSeventeenOpinion(String risendpSeventeenOpinion) {
		this.risendpSeventeenOpinion = risendpSeventeenOpinion;
	}

	public String getRisendpSeventeenInfo() {
		return this.risendpSeventeenInfo;
	}

	public void setRisendpSeventeenInfo(String risendpSeventeenInfo) {
		this.risendpSeventeenInfo = risendpSeventeenInfo;
	}

	public String getRisendpEighteenDept() {
		return this.risendpEighteenDept;
	}

	public void setRisendpEighteenDept(String risendpEighteenDept) {
		this.risendpEighteenDept = risendpEighteenDept;
	}

	public String getRisendpEighteenPost() {
		return this.risendpEighteenPost;
	}

	public void setRisendpEighteenPost(String risendpEighteenPost) {
		this.risendpEighteenPost = risendpEighteenPost;
	}

	public String getRisendpEighteenNo() {
		return this.risendpEighteenNo;
	}

	public void setRisendpEighteenNo(String risendpEighteenNo) {
		this.risendpEighteenNo = risendpEighteenNo;
	}

	public Date getRisendpEighteenTime() {
		return this.risendpEighteenTime;
	}

	public void setRisendpEighteenTime(Date risendpEighteenTime) {
		this.risendpEighteenTime = risendpEighteenTime;
	}

	public Date getRisendpEighteenActivetime() {
		return this.risendpEighteenActivetime;
	}

	public void setRisendpEighteenActivetime(Date risendpEighteenActivetime) {
		this.risendpEighteenActivetime = risendpEighteenActivetime;
	}

	public Date getRisendpEighteenInpartytime() {
		return this.risendpEighteenInpartytime;
	}

	public void setRisendpEighteenInpartytime(Date risendpEighteenInpartytime) {
		this.risendpEighteenInpartytime = risendpEighteenInpartytime;
	}

	public Date getRisendpEighteenObjtime() {
		return this.risendpEighteenObjtime;
	}

	public void setRisendpEighteenObjtime(Date risendpEighteenObjtime) {
		this.risendpEighteenObjtime = risendpEighteenObjtime;
	}

	public String getRisendpEighteenNoticestatus() {
		return this.risendpEighteenNoticestatus;
	}

	public void setRisendpEighteenNoticestatus(
			String risendpEighteenNoticestatus) {
		this.risendpEighteenNoticestatus = risendpEighteenNoticestatus;
	}

	public String getRisendpEighteenInfo() {
		return this.risendpEighteenInfo;
	}

	public void setRisendpEighteenInfo(String risendpEighteenInfo) {
		this.risendpEighteenInfo = risendpEighteenInfo;
	}

	public Date getRisendpNineteenTime() {
		return this.risendpNineteenTime;
	}

	public void setRisendpNineteenTime(Date risendpNineteenTime) {
		this.risendpNineteenTime = risendpNineteenTime;
	}

	public String getRisendpNineteenName() {
		return this.risendpNineteenName;
	}

	public void setRisendpNineteenName(String risendpNineteenName) {
		this.risendpNineteenName = risendpNineteenName;
	}

	public String getRisendpNineteenInfo() {
		return this.risendpNineteenInfo;
	}

	public void setRisendpNineteenInfo(String risendpNineteenInfo) {
		this.risendpNineteenInfo = risendpNineteenInfo;
	}

	public Date getRisendpTwentyTime() {
		return this.risendpTwentyTime;
	}

	public void setRisendpTwentyTime(Date risendpTwentyTime) {
		this.risendpTwentyTime = risendpTwentyTime;
	}

	public String getRisendpTwentyInfo() {
		return this.risendpTwentyInfo;
	}

	public void setRisendpTwentyInfo(String risendpTwentyInfo) {
		this.risendpTwentyInfo = risendpTwentyInfo;
	}

	public Date getRisendpTwentyoneTime() {
		return this.risendpTwentyoneTime;
	}

	public void setRisendpTwentyoneTime(Date risendpTwentyoneTime) {
		this.risendpTwentyoneTime = risendpTwentyoneTime;
	}

	public String getRisendpTwentyonePer() {
		return this.risendpTwentyonePer;
	}

	public void setRisendpTwentyonePer(String risendpTwentyonePer) {
		this.risendpTwentyonePer = risendpTwentyonePer;
	}

	public String getRisendpTwentyoneStatus() {
		return this.risendpTwentyoneStatus;
	}

	public void setRisendpTwentyoneStatus(String risendpTwentyoneStatus) {
		this.risendpTwentyoneStatus = risendpTwentyoneStatus;
	}

	public String getRisendpTwentyoneInfo() {
		return this.risendpTwentyoneInfo;
	}

	public void setRisendpTwentyoneInfo(String risendpTwentyoneInfo) {
		this.risendpTwentyoneInfo = risendpTwentyoneInfo;
	}

	public Date getRisendpTwentytwoTime() {
		return this.risendpTwentytwoTime;
	}

	public void setRisendpTwentytwoTime(Date risendpTwentytwoTime) {
		this.risendpTwentytwoTime = risendpTwentytwoTime;
	}

	public String getRisendpTwentytwoInfo() {
		return this.risendpTwentytwoInfo;
	}

	public void setRisendpTwentytwoInfo(String risendpTwentytwoInfo) {
		this.risendpTwentytwoInfo = risendpTwentytwoInfo;
	}

	public Date getRisendpTwentythreeTime() {
		return this.risendpTwentythreeTime;
	}

	public void setRisendpTwentythreeTime(Date risendpTwentythreeTime) {
		this.risendpTwentythreeTime = risendpTwentythreeTime;
	}

	public String getRisendpTwentythreeCount() {
		return this.risendpTwentythreeCount;
	}

	public void setRisendpTwentythreeCount(String risendpTwentythreeCount) {
		this.risendpTwentythreeCount = risendpTwentythreeCount;
	}

	public String getRisendpTwentythreeShoult() {
		return this.risendpTwentythreeShoult;
	}

	public void setRisendpTwentythreeShoult(String risendpTwentythreeShoult) {
		this.risendpTwentythreeShoult = risendpTwentythreeShoult;
	}

	public String getRisendpTwentythreePower() {
		return this.risendpTwentythreePower;
	}

	public void setRisendpTwentythreePower(String risendpTwentythreePower) {
		this.risendpTwentythreePower = risendpTwentythreePower;
	}

	public String getRisendpTwentythreeActual() {
		return this.risendpTwentythreeActual;
	}

	public void setRisendpTwentythreeActual(String risendpTwentythreeActual) {
		this.risendpTwentythreeActual = risendpTwentythreeActual;
	}

	public String getRisendpTwentythreeConsent() {
		return this.risendpTwentythreeConsent;
	}

	public void setRisendpTwentythreeConsent(String risendpTwentythreeConsent) {
		this.risendpTwentythreeConsent = risendpTwentythreeConsent;
	}

	public String getRisendpTwentythreeDisagree() {
		return this.risendpTwentythreeDisagree;
	}

	public void setRisendpTwentythreeDisagree(String risendpTwentythreeDisagree) {
		this.risendpTwentythreeDisagree = risendpTwentythreeDisagree;
	}

	public String getRisendpTwentythreeDefault() {
		return this.risendpTwentythreeDefault;
	}

	public void setRisendpTwentythreeDefault(String risendpTwentythreeDefault) {
		this.risendpTwentythreeDefault = risendpTwentythreeDefault;
	}

	public String getRisendpTwentythreeName() {
		return this.risendpTwentythreeName;
	}

	public void setRisendpTwentythreeName(String risendpTwentythreeName) {
		this.risendpTwentythreeName = risendpTwentythreeName;
	}

	public Date getRisendpTwentythreeMarktime() {
		return this.risendpTwentythreeMarktime;
	}

	public void setRisendpTwentythreeMarktime(Date risendpTwentythreeMarktime) {
		this.risendpTwentythreeMarktime = risendpTwentythreeMarktime;
	}

	public Date getRisendpTwentythreeLengthen() {
		return this.risendpTwentythreeLengthen;
	}

	public void setRisendpTwentythreeLengthen(Date risendpTwentythreeLengthen) {
		this.risendpTwentythreeLengthen = risendpTwentythreeLengthen;
	}

	public String getRisendpTwentythreeInfo() {
		return this.risendpTwentythreeInfo;
	}

	public void setRisendpTwentythreeInfo(String risendpTwentythreeInfo) {
		this.risendpTwentythreeInfo = risendpTwentythreeInfo;
	}

	public Date getRisendpTwentyfourTime() {
		return this.risendpTwentyfourTime;
	}

	public void setRisendpTwentyfourTime(Date risendpTwentyfourTime) {
		this.risendpTwentyfourTime = risendpTwentyfourTime;
	}

	public Date getRisendpTwentyfourBranch() {
		return this.risendpTwentyfourBranch;
	}

	public void setRisendpTwentyfourBranch(Date risendpTwentyfourBranch) {
		this.risendpTwentyfourBranch = risendpTwentyfourBranch;
	}

	public String getRisendpTwentyfourPerson() {
		return this.risendpTwentyfourPerson;
	}

	public void setRisendpTwentyfourPerson(String risendpTwentyfourPerson) {
		this.risendpTwentyfourPerson = risendpTwentyfourPerson;
	}

	public String getRisendpTwentyfourMarktime() {
		return this.risendpTwentyfourMarktime;
	}

	public void setRisendpTwentyfourMarktime(String risendpTwentyfourMarktime) {
		this.risendpTwentyfourMarktime = risendpTwentyfourMarktime;
	}

	public String getRisendpTwentyfourOpinion() {
		return this.risendpTwentyfourOpinion;
	}

	public void setRisendpTwentyfourOpinion(String risendpTwentyfourOpinion) {
		this.risendpTwentyfourOpinion = risendpTwentyfourOpinion;
	}

	public String getRisendpTwentyfourInfo() {
		return this.risendpTwentyfourInfo;
	}

	public void setRisendpTwentyfourInfo(String risendpTwentyfourInfo) {
		this.risendpTwentyfourInfo = risendpTwentyfourInfo;
	}

	public String getRisendpTwentyfiveAddress() {
		return this.risendpTwentyfiveAddress;
	}

	public void setRisendpTwentyfiveAddress(String risendpTwentyfiveAddress) {
		this.risendpTwentyfiveAddress = risendpTwentyfiveAddress;
	}

	public String getRisendpTwentyfiveInfo() {
		return this.risendpTwentyfiveInfo;
	}

	public void setRisendpTwentyfiveInfo(String risendpTwentyfiveInfo) {
		this.risendpTwentyfiveInfo = risendpTwentyfiveInfo;
	}

	public String getRisendpExpands1() {
		return this.risendpExpands1;
	}

	public void setRisendpExpands1(String risendpExpands1) {
		this.risendpExpands1 = risendpExpands1;
	}

	public String getRisendpExpands2() {
		return this.risendpExpands2;
	}

	public void setRisendpExpands2(String risendpExpands2) {
		this.risendpExpands2 = risendpExpands2;
	}

	public String getRisendpExpands3() {
		return this.risendpExpands3;
	}

	public void setRisendpExpands3(String risendpExpands3) {
		this.risendpExpands3 = risendpExpands3;
	}

	public String getRisendpExpands4() {
		return this.risendpExpands4;
	}

	public void setRisendpExpands4(String risendpExpands4) {
		this.risendpExpands4 = risendpExpands4;
	}

	public String getRisendpExpands5() {
		return this.risendpExpands5;
	}

	public void setRisendpExpands5(String risendpExpands5) {
		this.risendpExpands5 = risendpExpands5;
	}

	public String getRisendpExpands6() {
		return this.risendpExpands6;
	}

	public void setRisendpExpands6(String risendpExpands6) {
		this.risendpExpands6 = risendpExpands6;
	}

}