'use strict';

pupinoApp
    .config(function($stateProvider, $httpProvider, $translateProvider, USER_ROLES) {

        $stateProvider
            .state('global.project', {
                url: "/project",
                templateUrl: 'views/project/index.html',
                controller: 'ProjectController',
                resolve: {
                    resolvedProject: ['Project', function(Project) {
                        return Project.query().$promise;
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
            .state('global.project-detail', {
                url: "/project/:id",
                templateUrl: 'views/project/show.html',
                controller: 'ProjectDetailController',
                resolve: {
                    resolvedProject: ['Project', function(Project) {
                        return Project.get({id: 1}).$promise;
                    }]
                },
                access: {
                    authorizedRoles: [USER_ROLES.all]
                }
            })
    });
