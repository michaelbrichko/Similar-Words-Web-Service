# Similar-Words-Web-Service
A sample restful web service written on Java.

The web service has a database of english words and provides next services:
  1. Returns all words in the dictionary that has the same permutation as the word in the query. The word in the query won't be returned. 
  2. Return general statistics about the program: total number of words in the dictionary, total number of requests (not including "stats" requests), average time for request handling in nano seconds (not including "stats" requests).

API Examples:
  http://localhost:8000/api/v1/similar?word=test 
  {"similar":["sett","stet"]}
  
  http://localhost:8000/api/v1/stats
  {"totalWords":351075,"totalRequests":1,"avgProcessingTimeNs":80457807}

Technologies used:
  Server: Jetty
  Json serialization library: Gson
  
Implementation notes:
  The server has two servlets running inside of jetty, one for responding to "similar" requests and another one for responding for stats requests. The server has a cache mechanism in Similar servlet and caches some amount of responses. There is a BusinessLogic singleton object which is responsible for holding and updating statistics counters and it has the access to the database of words. Database of words is loaded in memory and was calculated on the server start up. The data structure that I've used is a hash map of key to a group of all permutations, meaning upon server start up it divides all the words to the groups where all words in each group are permutations one of another.

Here is how i calculate the key\candidate of permutations group:
  Each word can be viewed as an array of size 50 where there is 26 counters that count occurrences of every char in word.
For example: 
  abc: key = "1-1-1-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0"
  xyzz: key = "0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-1-1-2"
  cab: key = "1-1-1-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0"

Upon arrival of new request for similarities i "transform" the asked word to such key and look at the hash map for its presence, if it is not present it means that there are no permutations of asked word in the database, otherwise i retrieve the value of that key which is all permutations of the asked word. Then i validate that the group does not contain the asked word, If it contains i remove it from the group. The resulted group is being returned to the client.

Tests:
  Initially it runs 100000 sequential requests from a pool of 20000 words from words file. Later it runs 100000 concurrent requests from a pool of 20000 words and thread pool of 100. The concurrent test accumulates request words and their answers. On every response i check whether this kind of request was already executed and if so i validate their answers for equality. This check assures correct concurrent execution on the server side.
