package com.meijia.utils.htmlparse;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.meijia.utils.StringUtil;

public class JSoupUtil {

	public static final String USER_AGENT = "Mozilla";

	private static final int TIMEOUT = 3000;

	public static Document get(String url) throws IOException {
		// TODO: review user agent and timeout
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).get();
	}

	public static Document post(String url, Map<String, String> data) throws IOException {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).data(data).post();
	}
	
	/**
	 * 用jsoup 基础选择器，找出相应的值，范例如下：
	 * <meta charset="utf-8">
	 * <div class="resume-preview-main-title">
	 *	<div class="main-title-fl fc6699cc">xxxx</div>
	 *	<div class="main-title-fr">手机 ：13161070900</div>
	 *	</div>
	 *
	 *  找utf-8   JSoupUtil.parseByPatten(doc, "meta[charset]", "charset", "")
	 *  找xxxx  ，则为 JSoupUtil.parseByPatten(doc, "div.main-title-fl", "", "")
	 *  找手机号, 则为 JSoupUtil.parseByPatten(doc, "div.main-title-fr", "", "手机 ：")
	 * @param doc        jsoup Docuemnt
	 * @param patten     查找表达式
	 * @param attrName   属性
	 * @param replaceStr 最终结果替换的字符串
	 * @return
	 */
	public static String parseByPatten(Document doc, String patten, String attrName, String replaceStr) {
		String content = "";
		Element item = doc.select(patten).first();
		content = item.text();
		if (!StringUtil.isEmpty(attrName))
			content = item.attr(attrName);

		if (!StringUtil.isEmpty(replaceStr))
			content = content.replace(replaceStr, "");

		content.replace(" ", "");
		return content;
	}
	
	/**
	 * jsoup 解析标签后，用正则表达式进行分离，获得相应的值
	 * 
	 * <div class="summary-top">
			<span>女&nbsp;&nbsp;&nbsp;&nbsp;24岁(1991年4月)&nbsp;&nbsp;&nbsp;&nbsp;1年工作经验&nbsp;&nbsp;&nbsp;&nbsp;本科&nbsp;&nbsp;&nbsp;&nbsp;未婚</span>
			<br>
			现居住地：北京 | 户口：忻州 | 中共党员(含预备党员) | 有海外工作/学习经验
		</div>
		
		JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "/男|女")
		女    24岁(1991年4月)    1年工作经验    本科    未婚
	 * @param doc        jsoup Docuemnt
	 * @param patten     查找表达式
	 * @param attrName   属性
	 * @param regex 	 查找的正则表达式
	 * @return
	 */
	public static String parseByPattenAndSinglRegex(Document doc, String patten, String attrName, String regex) {

		String result = "";
		String content = "";
		Element item = doc.select(patten).first();
		content = item.text();
		if (!StringUtil.isEmpty(attrName))
			content = item.attr(attrName);
		
//		System.out.println(content);
		try {
			Pattern pat = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher mat = pat.matcher(content);

			boolean rs = mat.find();

			if (!rs)
				return result;

			result = mat.group(0);
		} catch (Exception e) {
			return "";
		}

		return result;
	}
	
	/**
	 * jsoup 解析标签后，用正则表达式进行分离，获得相应的值，才有左右标签的形式
	 * <div class="summary-top">
			<span>女&nbsp;&nbsp;&nbsp;&nbsp;24岁(1991年4月)&nbsp;&nbsp;&nbsp;&nbsp;1年工作经验&nbsp;&nbsp;&nbsp;&nbsp;本科&nbsp;&nbsp;&nbsp;&nbsp;未婚</span>
			<br>
			现居住地：北京 | 户口：忻州 | 中共党员(含预备党员) | 有海外工作/学习经验
		</div>
		
		JSoupUtil.parseByPattenAndBetweenRegex(doc, "div.summary-top", "", "现居住地", "|", "：")
	 * @param doc
	 * @param patten
	 * @param attrName
	 * @param leftRegex
	 * @param rightRegex
	 * @param replaceStr
	 * @return
	 */
	public static String parseByPattenAndBetweenRegex(Document doc, String patten, String attrName, String leftRegex, String rightRegex, String replaceStr) {

		String result = "";
		String content = "";
		Element item = doc.select(patten).first();
		content = item.text();
		if (!StringUtil.isEmpty(attrName))
			content = item.attr(attrName);
//		System.out.println(content);
		try {
			Matcher mat = Pattern.compile(Pattern.quote(leftRegex) + "(.*?)" + Pattern.quote(rightRegex)).matcher(content);

			boolean rs = mat.find();

			if (!rs)
				return result;

			result = mat.group(1);

			if (!StringUtil.isEmpty(replaceStr))
				result = result.replace(replaceStr, "");

		} catch (Exception e) {
			return "";
		}

		return result;
	}
	
	public static String parseTableByPatten(Document doc, String patten, String tdFlag, int tdValueIndex) {

		String result = "";
		Element table = doc.select(patten).get(0); //select the first table.
		
		Elements rows = table.select("tr");
		
		for (int i = 0; i < rows.size(); i++) { 
			Element row = rows.get(i);
	        Elements cols = row.select("td");
//	        System.out.println("tr ====================");
	        for (int j = 0; j < cols.size(); j++) {
//	        	System.out.println("td ====" + cols.get(j).text());
	        	if (cols.get(j).text().trim().equals(tdFlag.trim())) {
	        		result = cols.get(tdValueIndex).text();
	        		break;
	        	}
	        }
	        
		}

		return result;
	}	

	/**
	 * 转义正则特殊字符 （$()*+.[]?\^{},|）
	 * 
	 * @param keyword
	 * @return
	 */
	public static String escapeExprSpecialWord(String keyword) {
		if (StringUtils.isEmpty(keyword)) {
			String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
			for (String key : fbsArr) {
				if (keyword.contains(key)) {
					keyword = keyword.replace(key, "\\" + key);
				}
			}
		}
		return keyword;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File input = new File("/Users/lnczx/Downloads/resume-test.html");
		Document doc = Jsoup.parse(input, "UTF-8", "");

		// Element item = doc.select("meta[charset]").first();
		// System.out.println(item);
		// String item1 = item.attr("charset");
		// System.out.print(item1);
		
		System.out.println("match chartset = " + JSoupUtil.parseByPatten(doc, "meta[charset]", "charset", ""));
		System.out.println("match ID = " + JSoupUtil.parseByPatten(doc, "span.resume-left-tips-id", "", "ID:"));
		System.out.println("match 姓名 = " + JSoupUtil.parseByPatten(doc, "div.main-title-fl", "", ""));
		System.out.println("match 手机号 = " + JSoupUtil.parseByPatten(doc, "div.main-title-fr", "", "手机 ："));
		System.out.println("match 性别 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "/男|女"));
		System.out.println("match 生日 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "(([0-9]+年[0-9]+月))"));
		System.out.println("match 工作经验 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "(([0-9]+年工作经验))"));
		System.out.println("match 学历 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "/小学|初中|高中|大专|专科|本科|硕士|博士|博士后"));
		System.out.println("match 婚姻状态 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top > span", "", "/已婚|未婚|保密|离异"));
		System.out.println("match 现居住地 = " + JSoupUtil.parseByPattenAndBetweenRegex(doc, "div.summary-top", "", "现居住地", "|", "："));
		System.out.println("match 户口 = " + JSoupUtil.parseByPattenAndBetweenRegex(doc, "div.summary-top", "", "户口", "|", "："));
		System.out.println("match 政治面貌 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top", "", "中共党员\\(含预备党员\\) |团员|群众|民主党派|无党派人士|无可奉告"));
		System.out.println("match 海外经验 = " + JSoupUtil.parseByPattenAndSinglRegex(doc, "div.summary-top", "", "有海外工作\\/学习经验"));
		System.out.println("match 身份证 = " + JSoupUtil.parseByPattenAndBetweenRegex(doc, "div.summary-bottom", "", "身份证：", " ", ""));
		System.out.println("match 手机 = " + JSoupUtil.parseByPattenAndBetweenRegex(doc, "div.summary-bottom", "", "手机：", " ", ""));
		System.out.println("match 邮箱 = " + JSoupUtil.parseByPatten(doc, "a[href^=mailto]", "", ""));
		System.out.println("match 头像 = " + JSoupUtil.parseByPatten(doc, "img.headerImg", "src", ""));
		System.out.println("match 期望工作地区 = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","期望工作地区：", 1));
		System.out.println("match 期望月薪： = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","期望月薪：", 1));
		System.out.println("match 目前状况： = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","目前状况：", 1));
		System.out.println("match 期望工作性质： = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","期望工作性质：", 1));
		System.out.println("match 期望从事职业： = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","期望从事职业：", 1));
		System.out.println("match 期望从事行业： = " + JSoupUtil.parseTableByPatten(doc, "div.resume-preview-top > table","期望从事行业：", 1));
		System.out.println("match 自我评价 = " + JSoupUtil.parseByPatten(doc, "div.resume-preview-dl","", ""));
		
		
		
	}

}
