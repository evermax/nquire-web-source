module.exports = function(grunt) {
	grunt.initConfig({
		sass: {
			options: {
				sourceMap: true
			},
			dist: {
				files: {
					'static/src/css/nquire-it-bootstrap.css':
						'static/sass/nquire-it/nquire-it-bootstrap.scss'
				}
			}
		},
		jshint: {
			options: {
				'-W097': true,  // Ignore position of 'use strict';
				globals: {
					angular: true
				}
			},
			config: 'static/src/js/app/config.js',
			src: 'static/src/js/app/controllers/**/*.js'
		},

		nggettext_extract: {
			pot: {
				// Translate strings in Javascript [Bug: #23]
				options: {
					markerNames: [ '_' ]
				},
				files: {
					'po/template.pot': [
						'static/**/*.html',
						'static/**/*.js'
					]
				}
			}
		},
		nggettext_compile: {
			all: {
				files: {
					'static/src/js/app/translations.js': [
						'po/*.po'
					]
				}
			}
		},
		msgInitMerge: {
			your_target: {
				src: [
					'po/template.pot'
				],
				options: {
					// I18n / translation - list available languages, except 'en' [Bug: #3]
					'locales': [
						'el',
						'es',
						'fr'
					],
					poFilesPath: 'po/<%= locale %>.po'
				}
			}
		}
	});

	grunt.loadNpmTasks('grunt-angular-gettext');
	grunt.loadNpmTasks('grunt-msg-init-merge');
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-nice-package');
	grunt.loadNpmTasks('grunt-sass');

	grunt.registerTask('gettext', [
		'nggettext_extract',
		'nggettext_compile',
		'msgInitMerge'
	]);

	grunt.registerTask('default', [ 'gettext', 'sass', 'jshint:config', 'nice-package' ]);
};
