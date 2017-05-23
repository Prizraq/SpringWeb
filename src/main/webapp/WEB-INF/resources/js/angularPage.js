var ZonaModeration = ZonaModeration || {};
ZonaModeration.TagsForm = function(){
	
    var app = angular.module('angularPageModule', []);

    var controller = app.controller('AngularPageModuleController', function($scope, $http) {

    	$scope.load = function() {
            $http.post('/SpringWeb/angular/get').
            success(function(data, status, headers, config) {
                if (data.error) {
                    alert(data.error);
                } else {
                	$scope.list = data.data.list;
                }
            }).
            error(function(data, status, headers, config) {
                alert(status);
            });
    	};
    	
    	$scope.clickButton = function() {
    		$http.post('/SpringWeb/angular/save', {text : $scope.textField}).
            success(function(data, status, headers, config) {
                if (data.error) {
                    alert(data.error);
                } else {
                	
                }
            }).
            error(function(data, status, headers, config) {
                alert(status);
            });
    	};

    	
    });
    
	return {
	};
}();