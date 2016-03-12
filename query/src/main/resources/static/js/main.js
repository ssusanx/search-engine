

var angularApp = angular.module('search', []);
angularApp.controller('home', ['$scope', '$http', function ($scope, $http) {


    $scope.getSearchResult = function(term){
	    $http.get('http://localhost:9091/search?term='+term).
        success(function(data) {
            $scope.results = data;
        });
    }

}]);