<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ include file="../shared/taglib.jsp"%>

<html>
<head>
<title>资产管理--库存管理</title>

<!--common css for all pages-->
<%@ include file="../shared/importCss.jsp"%>
<!--css for this page-->
<link href="<c:url value='/assets/js/amazeui.datatables/amazeui.datatables.min.css'/>"
	rel="stylesheet">

</head>

<body>
	<!--header start-->
	<%@ include file="../shared/pageHeader.jsp"%>
	<!--header end-->

	<div class="am-cf admin-main">
		<!-- sidebar start -->
		<%@ include file="../xz/xz-menu.jsp"%>
		<!-- sidebar end -->

		<!-- content start -->
		<div class="admin-content">

			<div class="am-cf am-padding">
				<div class="am-fl am-cf">
					<strong class="am-text-primary am-text-lg">库存管理列表</strong>
				</div>
			</div>
			<hr>
			
			
			<div class="am-g">
				<div class="am-u-sm-12 am-u-md-6">
					<div class="am-btn-toolbar">
						<div class="am-btn-group am-btn-group-sm">
							<button type="button" id="btn-comm-asset-add" class="am-btn am-btn-default am-radius">
								<span class="am-icon-plus"></span> 入库登记
							</button>
						</div>
					</div>
				</div>

				<div class="am-u-sm-12 am-u-md-3">
					<!-- <form class="am-form" id="search-form"> -->
					 <form:form modelAttribute="searchVoModel" action="commpany_asset_list" method="GET">
						<div class="am-input-group am-input-group-sm">
							<input type="text" name="name" 
								   class="am-form-field" placeholder="资产名称" maxLength="18" > 
								
								<span class="am-input-group-btn">
									<button class="am-btn am-btn-default" type="submit">搜索</button>
								</span>
						</div>
					</form:form>
				</div>
			</div>
			
			
			<br>
				<div class="am-g">
				<div class="am-u-sm-12">
					<form class="am-form">
						<table id="list-table" class="am-table am-table-bordered am-table-striped">
							<thead>
								<tr>
									<th class="table-title">资产名称</th>
									<th class="table-title">编号</th>
									<th class="table-title">库存</th>
									<th class="table-title">规格</th>
									<th class="table-title">单价</th>
									<th class="table-title">添加时间</th>
									<th >操作</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${contentModel.list}" var="item">
									<tr>
										<td>${item.name}</td>
										<td>${item.seq }</td>
										<td>${item.stock}</td>
										<td>${item.unit }</td>
										<td>${item.price}</td>
										<td>${item.addTimeStr }</td>
										<td>
											<button type="button" class="am-btn am-btn-success" 
											
											onclick="getAssetDetail(${item.assetId},'${item.name }')">查看详情</button>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
						<c:import url="../shared/paging.jsp">
							<c:param name="pageModelName" value="contentModel" />
							<c:param name="urlAddress" value="/xz/assets/commpany_asset_list" />
						</c:import>
					</form>
				</div>
			</div>
		</div>

		</div>

	</div>

	<a href="#" class="am-icon-btn am-icon-th-list am-show-sm-only admin-menu"
		data-am-offcanvas="{target: '#admin-offcanvas'}"></a>


	<!--footer start-->
	<%@ include file="../shared/pageFooter.jsp"%>
	<!--footer end-->

	<!-- js placed at the end of the document so the pages load faster -->
	<!--common script for all pages-->
	<%@ include file="../shared/importJs.jsp"%>

	<!--script for this page-->

	<script src="<c:url value='/assets/js/amazeui.datatables/amazeui.datatables.min.js'/>"></script>
	<script src="<c:url value='/assets/js/amazeui.datatables/dataTables.responsive.min.js'/>"></script>
	<script src="<c:url value='/assets/js/xcloud/xz/commpany-asset-list.js'/>"></script>
</body>
</html>
