


angular.module('senseItWeb', null, null).controller('ProjectEditSenseItCtrl', function ($scope) {
    console.log('senseit create controller');

    $scope.$watch('project', function() {
        $scope.activity = $scope.project.activity;
    });
});

