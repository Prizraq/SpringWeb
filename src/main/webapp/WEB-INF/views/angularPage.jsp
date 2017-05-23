<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Пробная страничка с ангуляром</title>
    <link type="text/css" rel="stylesheet" href="resources/css/bootstrap.min.css" />
    <script charset="UTF-8" type="text/javascript" src="resources/js/lib/angular.min.js"></script>
    <script charset="UTF-8" type="text/javascript" src="resources/js/lib/underscore-min.js"></script>
	<script charset="UTF-8" type="text/javascript" src="resources/js/angularPage.js"/>"></script>
</head>
<body ng-app="angularPageModule" ng-controller="AngularPageModuleController" data-ng-init="load()">
	<div class="container-fluid">
		<div class="row" ng-repeat="l in list">
			<div class="col-md-1">
				{{l.key}}
			</div>
			<div class="col-md-2">
				{{l.value}}
			</div>
		</div>
		<div style="height: 20px;"></div>
		<div class="row">
			<div class="col-md-3">
				<input type="text" ng-model="textField" class="form-control"/>
			</div>
			<div class="col-md-1">
				<button ng-click="clickButton()" class="btn btn-default btn-lg">Нажать!</button>
			</div>
		</div>
	</div>
</body>
</html>

