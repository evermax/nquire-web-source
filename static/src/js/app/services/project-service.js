'use strict';

angular.module('senseItServices', null, null).factory('ProjectService', ['RestService', 'OpenIdService', function (RestService, OpenIdService) {

    var utils = {
        composePath: function (path, suffix) {
            return path + (suffix ? '/' + suffix : '');
        },
        /**
         *
         * @param method
         * @param path
         * @param data
         * @param [files=null]
         * @returns {*}
         * @private
         */
        request: function (method, path, data, files) {
            return data ? RestService[method](path, data, files) : RestService[method](path);
        },
        /**
         *
         * @param method
         * @param projectId
         * @param [path=null]
         * @param [data=null]
         * @param [files=null]
         * @returns {*}
         */
        projectRequest: function (method, projectId, path, data, files) {
            var _path = utils.composePath('api/project/' + projectId, path);
            return utils.request(method, _path, data, files);
        },

        /**
         *
         * @param method
         * @param [path=null]
         * @param [data=null]
         * @returns {*}
         */
        projectsRequest: function (method, path, data) {
            var _path = this.composePath('api/projects', path);
            return this.request(method, _path, data);
        }
    };


    var ProjectListWatcher = function (scope, callback) {
        var self = this;

        this.data = {projects: [], categories: {}, ready: true};
        this._query = false;

        scope.projectList = this.data;

        var destroyWatch = scope.$watch('projectList', callback, true);

        var openIdListener = function () {
            self._reload();
        };

        scope.$on('$destroy', function () {
            OpenIdService.removeListener(openIdListener);
            destroyWatch();
        });
        OpenIdService.registerListener(openIdListener);
    };

    ProjectListWatcher.prototype._reload = function () {
        this.data.ready = false;
        var self = this;
        RestService.get('api/projects').then(function (data) {
            self.data.ready = true;
            self.data.projects = data.list;
            self.data.categories = data.categories;
        });
    };


    ProjectListWatcher.prototype.query = function (query) {
        this._query = query;
        this._reload();
    };

    var ProjectWatcher = function (scope, projectId) {
        var self = this;
        this.projectId = projectId;

        this.data = {project: null, access: null, data: null, ready: false};

        scope.projectData = this.data;

        var destroyWatch = scope.$watch('projectData', null, true);

        var openIdListener = function () {
            self._reload();
        };

        scope.$on('$destroy', function () {
            OpenIdService.removeListener(openIdListener);
            destroyWatch();
        });

        OpenIdService.registerListener(openIdListener);

        this._reload();
    };

    ProjectWatcher.prototype._reload = function () {
        this.data.ready = false;
        var self = this;
        utils.projectRequest('get', this.projectId).then(function (data) {
            self.data.ready = true;
            self.data.project = data.project;
            self.data.access = data.access;
            self.data.data = data.data;
        });
    };


    ProjectWatcher.prototype._subscriptionAction = function (action) {
        var self = this;
        return utils.projectRequest('post', this.projectId, action).then(function (data) {
            if (data) {
                self.data.access = data;
            }
            return true;
        });
    };

    ProjectWatcher.prototype.joinProject = function () {
        return this._subscriptionAction('join');
    };

    ProjectWatcher.prototype.leaveProject = function () {
        return this._subscriptionAction('leave');
    };


    ProjectWatcher.prototype.deleteProject = function (projectId) {
        var self = this;
        return utils.projectRequest('delete', this.projectId).then(function (deleted) {
            if (deleted) {
                self.data.project = null;
                self.data.access = null;
                self.data.data = null;
                self.data.ready = true;
            }
            return deleted;
        });
    };

    /**
     *
     * @param {string} projectId
     * @param {string} method
     * @param {object} path
     * @param {object} [data=null]
     * @param {object} [files=null]
     * @returns {object}
     * @private
     */
    ProjectWatcher.prototype._updateProjectAction = function (method, path, data, files) {
        var self = this;
        return utils.projectRequest(method, this.projectId, path, data, files).then(function (data) {
            self.data.ready = true;
            self.data.project = data.project;
            self.data.access = data.access;
            self.data.data = data.data;

            return true;
        });
    };

    /**
     *
     * @param {string} projectId
     * @param {object} [files=null]
     * @returns {object}
     * @private
     */
    ProjectWatcher.prototype.saveMetadata = function (files) {
        return this._updateProjectAction('post', 'metadata', {
            title: this.data.project.title,
            description: this.data.project.description
        }, files);
    };


    ProjectWatcher.prototype.openProject = function () {
        return this._updateProjectAction('put', 'admin/open');
    };
    ProjectWatcher.prototype.closeProject = function () {
        return this._updateProjectAction('put', 'admin/close');
    };

    ProjectWatcher.prototype.getUsers = function () {
        return utils.projectRequest('get', this.projectId, 'admin/users');
    };


    return {
        watchList: function (scope, callback) {
            scope.projectListWatcher = new ProjectListWatcher(scope, callback || null);
        },
        watchProject: function (scope, projectId) {
            scope.projectWatcher = new ProjectWatcher(scope, projectId);
        },
        createProject: function (data) {
            return utils.projectsRequest('post', false, data);
        }
    };


}]);
