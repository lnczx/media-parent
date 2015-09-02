<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ include file="../shared/taglib.jsp"%>

<!--taglib for this page  -->
<%@ taglib prefix="timestampTag" uri="/WEB-INF/tags/timestamp.tld" %>
<%@ taglib prefix="spiderPartnerStatusSelectTag" uri="/WEB-INF/tags/spiderPartnerStatusSelect.tld" %>
<%@ taglib prefix="partneCompanySizeSelectTag" uri="/WEB-INF/tags/partnerCompanySizeSelect.tld" %>
<%@ taglib prefix="partneIsCooperateSelectTag" uri="/WEB-INF/tags/partnerIsCooperateSelect.tld" %>
<%@ taglib prefix="spiderPartnerStatusTag" uri="/WEB-INF/tags/spiderPartnerStatus.tld"%>
<%@ taglib prefix="partneCompanySizeTag" uri="/WEB-INF/tags/partnerCompanySize.tld"%>
<%@ taglib prefix="partneIsCooperate" uri="/WEB-INF/tags/partnerIsCooperate.tld"%>
<html>
  <head>
	
	<!--common css for all pages-->
	<%@ include file="../shared/importCss.jsp"%>
	
	<!--css for this page-->

  </head>

  <body>

  <section id="container" >
	 
	  <!--header start-->
	  <%@ include file="../shared/pageHeader.jsp"%>
	  <!--header end-->
	  
      <!--sidebar start-->
	  <%@ include file="../shared/sidebarMenu.jsp"%>
      <!--sidebar end-->
      
      <!--main content start-->
      <section id="main-content">
          <section class="wrapper">
              <!-- page start-->
				<%@ include file="../common/partner/partnersSearch.jsp"%>
              <div class="row">
                  <div class="col-lg-12">
                      <section class="panel">
                          <header class="panel-heading">
                          	服务商列表
                          </header>
                          <hr style="width: 100%; color: black; height: 1px; background-color:black;" />
                          

                          <table class="table table-striped table-advance table-hover">
                              <thead>
                              <tr>
                                	  <th >公司名称</th>
		                              <th >公司规模</th>
		                              <th >注册时间</th>
		                              <th >合作方式</th>
		                              <th>采集url</th>
		                              <th>状态</th>
		                              <th >最后操作时间</th>
		                              <th>操作</th>
                              </tr>
                              </thead>
                              <tbody>
                              <c:forEach items="${contentModel.list}" var="item">
                              <tr>
	                                   <td>${ item.companyName }</td>
							            <td>
							            <partneCompanySizeTag:companySize companySize="${ item.companySize }"/></td>
							            <td>${ item.registerTime }</td>
								        <td>
								           <partneIsCooperate:isCooperate isCooperate="${ item.isCooperate }"/>
								         </td>
								        <td>
								           <a href="${ item.spiderUrl }" target="_blank">采集内容链接</a>
								         </td>
							            <td>
							            	<spiderPartnerStatusTag:status status="${item.status }"/>
							            </td>
							            <td>
							               <timestampTag:timestamp patten="yyyy-MM-dd" t="${item.updateTime * 1000}"/>
							            </td>
							            <td>
							            	<button id="btn_update"  onClick="btn_update('partners/partnerForm?id=${ item.spiderPartnerId }')" class="btn btn-primary btn-xs" title="修改"><i class="icon-pencil"></i></button>
<%-- 	                                  		<button id="btn_del" onClick="btn_del('/account/delete/${item.id}')" class="btn btn-danger btn-xs"  title="删除"><i class="icon-trash "></i></button>
 --%>							            </td>
                              </tr>
                              </c:forEach>
                              </tbody>
                          </table>

                          
                      </section>
                      
                      <c:import url = "../shared/paging.jsp">
	        				<c:param name="pageModelName" value="contentModel"/>
	        				<c:param name="urlAddress" value="/partners/list"/>
	       			  </c:import>
                  </div>
              </div>
              <!-- page end-->
          </section> 
      </section>
      <!--main content end-->
      
      <!--footer start-->
      <%@ include file="../shared/pageFooter.jsp"%>
      <!--footer end-->
  </section>

    <!-- js placed at the end of the document so the pages load faster -->
    <!--common script for all pages-->
    <%@ include file="../shared/importJs.jsp"%>

    <!--script for this page-->	
	<script type="text/javascript">
	function doReset(){
		$("#companyName").attr('value',' ');
	}
	</script>

  </body>
</html>