Docker
=================

This project if forked from nQuire-it. Its aim is to provide integration with the [TinCan](https://tincanapi.com) standard to communicate learning records to a [LRS](https://tincanapi.com/learning-record-store/).

This has been made during my last year at [Télécom Bretagne](http://www.telecom-bretagne.eu/index.php?lang=en_GB) as a project.

I dockerized the project, added a few things like sending emails on another thread and sending the logs to a [Sentry](getsentry.com) instance and then worked on the integration of the TinCan standard using the library [jxapi](http://github.com/adlnet/jxapi).

The structure of the project is now the following:

![structure](nquire.png)

The work on the app Sense-it will be published on a fork of the corresponding repo, but the main change will be to add the French translation, as all the changes about LRS communications will be on the API side.

### Commands

#### Setup

Clone the directory, rename all the files with .example by removing the `.example` extension.

Fill in the files with `.env` extension.

- `mysql.env` contains all the informations about the MySQL database (used by nquire as well):
	- `MYSQL_ROOT_PASSWORD`: MySQL root password
	- `MYSQL_USER`: MySQL username that will be created to avoid using root user.
	- `MYSQL_PASSWORD`: MySQL password for the previous user.
	- `MYSQL_DATABASE`: The name of the database to be created. The create will have full access for the created user.
- `psql.env` contains all the informations about the PostgreSQL database:
	- `POSTGRES_USER`: The user to be created.
	- `POSTGRES_PASSWORD`: The password for this user.
	- `PGDATA=/var/lib/postgresql/data/pgdata`: It is recommended not to use the default path to store the psql database info.
	- `POSTGRES_DB=lrs`: Another database to be create in addition to `postgres`. The name should stay the same for the LRS part to work.
- `lrs.env` environment variables for the LRS image:
	- `LRS_ADMIN_NAME`: Admin name
	- `LRS_ADMIN_PASS`: Admin password
	- `LRS_ADMIN_MAIL`: Admin mail
	- `LRS_SECRET_KEY`: Some long random string with numb3rs and $ymbol$
- `rabbit.env` environment variables for the rabbitmq image (used by the LRS as well):
	- `RABBITMQ_DEFAULT_USER`: Rabbitmq name
	- `RABBITMQ_DEFAULT_PASS`: Rabbitmq password
- `sentry.env`
	- `SECRET_KEY`
	- `SENTRY_URL_PREFIX`: http://example.com, this must be the url after proxying.
	- `DATABASE_URL`: postgres://user:pwd@postgres/dbname
	- `SENTRY_ADMIN_USERNAME` Admin username
	- `SENTRY_ADMIN_PASSWORD` Admin password
	- `SENTRY_ADMIN_EMAIL` Admin email
	- `SENTRY_INITIAL_TEAM` Name of the team to be displayed on Sentry
	- `SENTRY_INITIAL_PROJECT` Name of the project to be displayed on Sentry
	- `SENTRY_INITIAL_KEY` publickey:secretkey
	- `SENTRY_USE_REDIS_TSDB` leave this value to True or check [the repo of the image](https://github.com/slafs/sentry-docker)


#### Launch the project

```
docker-compose up -d
```

A long process is going to start, you will need to wait quite a lot. You can remove the `-d` the first time to see how long it takes and have an idea about what is happening. There are some errors but most of them are quite normal.
Considering the `nquire` container has dependencies from all other modules and is quite long to launch, you should check the logs using `docker-compose log nquire`, it will give you an idea if the project is launched or not.

If there is any problem the very first time you launch it, stop everything : `docker-compose down`

And start it again: `docker-compose up -d`.

There are some weird issue the first time when populating the database for the first time and I can't really put my hands on it.

#### Access the database

If you want to access the database for various reasons, you can always do something like this:

```
docker run -it --rm --link nquirewebsource_mysql_1:mysql --net=nquirewebsource_back mysql/mysql-server sh -c 'exec mysql -h mysql -P 3306 -u root -p'
```

/!\ The prefix `nquirewebsource_` comes from the folder name it's in. Here it is the name of the repo, the default one you get when you clone `nquire-web-source` but if you change the name of the folder, it will change.

Then enter the root password that you provided inside `mysql.env`.


#### Make backups of the data volumes:

This is quite important in order not to loose your data.

We can image having those scripts in a cron script.

```
docker run --rm \
-v nquirewebsource_mysql_data:/var/lib/mysql \
-v "$(pwd)":/backups \
-ti ubuntu \
tar cvzf /backups/docker-mysql-`date +%y%m%d-%H%M%S`.tgz /var/lib/mysql
```

```
docker run --rm \
-v nquirewebsource_psql_data:/var/lib/postgresql/data/pgdata \
-v "$(pwd)":/backups \
-ti ubuntu \
tar cvzf /backups/docker-psql-`date +%y%m%d-%H%M%S`.tgz /var/lib/postgresql/data/pgdata
```

```
docker run --rm \
-v nquirewebsource_mail_data:/var/mail \
-v "$(pwd)":/backups \
-ti ubuntu \
tar cvzf /backups/docker-mailserver-`date +%y%m%d-%H%M%S`.tgz /var/mail
```
