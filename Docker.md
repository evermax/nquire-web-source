Docker
=================

This project if forked from nQuire-it. Its aim is to provide integration with the [TinCan](https://tincanapi.com) standard to communicate learning records to a [LRS](https://tincanapi.com/learning-record-store/).

This has been made during my last year at [Télécom Bretagne](http://www.telecom-bretagne.eu/index.php?lang=en_GB) as a project.

I dockerized the project, added a few things like sending emails on another thread and sending the logs to a [Sentry](getsentry.com) instance and then worked on the integration of the TinCan standard using the library [jxapi](http://github.com/adlnet/jxapi).

The structure of the project is now the following:

![structure](nquire.png)

The work on the app Sense-it will be published on a fork of the corresponding repo, but the main change will be to add the French translation, as all the changes about LRS communications will be on the API side.

### Setup

Clone the directory, rename all the files with .example by removing the `.example` extension.

#### Fill in the files with `.env` extension.

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


#### Set up the properties of the Java project:

In `app/src/main/webapp/` you need to copy `nquireit.properties.default` into `nquireit.properties` and complete the properties as follow:

If you are using docker, leave it like that. If not, you'll probably know what to put there then!

```
database.url=jdbc:mysql://mysql:3306
persistence.unit=nquire-it-jpa
```

Those need to be set if you are using a proxy.
Leave it empty otherwise, but don't remove it or the server will not start.

```
server.proxyHost=http://localhost
server.proxyPort=8000
```

This is the front url on which you will deploy nquire.

```
app.url=
```

Some infos when sending emails.

```
mail.sender=
mail.name=
```

This is some details for the password storage.

```
security.encodingSecret=secret
security.encryptPassword=password
security.encryptSalt=1234567890
```

Get them from Facebook if you want to use Facebook as a provider

```
facebook.clientId=id
facebook.clientSecret=secret
```

Get them from Twitter if you want to use Twitter as a provider

```
twitter.consumerKey=id
twitter.consumerSecret=secret
```

Get them from Google if you want to use Google as a provider.
As a side note for this one: users without Google as a provider linked to their account
won't be able to send informations from the app.

```
google.consumerKey=key
google.consumerSecret=secret
```

reCaptcha site key

```
recaptcha.siteKey=key
recaptcha.secretKey=secret
```

#### Default language

The default language is english but you can change it by changing the `default_language` value in the file `static/src/js/app/config.js` (first you need to copy `static/src/js/app/config.js.DIST.html` into `static/src/js/app/config.js`)


### Build java project using maven

In order to get all the dependencies for the project, you need to run maven. Now, it wouldn't be any good to use Docker to avoid installing java (among other nice features) if you need maven on the server, so java. There is a good way to use docker for that as well!

Just run the following, in this folder:

First, run this just once:

```
docker run --name maven_data -v /root/.m2 busybox echo 'data for maven'
```

It will create a data volume container that will contain all the dependencies for this project.
That way you won't have to download the dependencies when you want to build the project, but when you want to remove the project, you remove this data container as well and you won't have them either.

Then, every time you want to build the java project (which is needed after every change on it, for example after each pull), just do:

```
docker run --rm -v $(pwd)/app:/project --volumes-from maven_data dirichlet/maven clean install -DskipTests
```

You can make an alias out of it to make you life easier like:

```
alias maven="docker run --rm -v $(pwd)/app:/project --volumes-from maven_data dirichlet/maven clean install -DskipTests"
```

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
