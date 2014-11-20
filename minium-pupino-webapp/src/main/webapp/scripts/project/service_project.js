'use strict';

pupinoApp
    .factory('Project', function($resource) {
        return $resource('app/rest/projects/:id', {}, {
            'query': {
                method: 'GET',
                isArray: true
            },
            'get': {
                method: 'GET'
            }
        });
    })
    .factory('JenkinsProvider', ['$resource', '$http', function($resource, $http) {
        return {
            builds: $resource('app/rest/jenkins/builds/:jobName', {}, {
                query: {
                    method: 'GET',
                    isArray: true
                }
            }),
            createBuild: function(project) {
                return $http.post('app/rest/jenkins/builds/create/' + project.name, {});
            }
        };
    }]);
