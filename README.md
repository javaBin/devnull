

Setup
=====
* createuser -P devnull
* createdb devnull
* sbt run

Running Tests
=============

Run unit test

````
sbt test
````

Test that is tagged as database test. Require that you have a postgres database

````
sbt db:test
````

Unit test, database test and test that is tagged as slow.

````
sbt all:test
````

Examples
========

* Add feedback
````
curl 'http://localhost:8082/server/events/1234/sessions/5678/feedbacks' -H 'Content-Type: application/json' --data-binary $'{\n  "template": {\n "data": [\n      {"name": "overall", "value" : 1},\n      {"name": "relevance", "value" : 1},\n      {"name": "content", "value" : 1},\n {"name": "quality", "value" : 1},\n      ]\n  }\n}'
````
