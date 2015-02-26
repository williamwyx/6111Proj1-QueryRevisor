=====================================================
a. Name and UNI

Name: Qiuyang Shen, Yuxuan Wang				
UNI: qs2147, yw2666

=====================================================
b. File Structure
Below is the file structure in directory hw1

├── bin
│   └── commons-codec-1.10
│       └── commons-codec-1.10.jar
├── bing
│   ├── BingTest.java
│   ├── StopWordsHelper.java
│   └── VectorSpaceModel.java
├── build.sh
├── run.sh
└── stopwords.txt


=====================================================
c. How to compile and run

1.Enter into the directory hw1
2.To compile, type:  ./build.sh
3.To run, type : ./<bing account key> <precision> <query>


=====================================================
d. Description of internal design


VectorModelModel.java
	1. Remove non-alphabetic character
	2. Remove stopwords
	3. Calculate custom "term frequency" 
	4. Rank the words by tf in ascending order
	5. Add top 2 words which is not in original query to new query, reorder words in new query. 
	6. return the new query

StopWordHelper.java
	It is a singleton class. It reads stopwords from file 'stopwords.txt' and store them in a HashSet. 


=====================================================
e. Description of query modification method
1. We only use term frequency to indicate the weight of words in each document. The reason we choose tf instead of tf-idf is the number of documents is only ten and data volume is not large. The effect of tf better than tf-idf. 
2. We increase the weight of word whose adjancent word in query to 2. For example, the query is "columbia", a document contains "columbia university in the city of new york........". We count the number of appearance of word "university" as 2 because its adjancent word "columbia" is in the query. 
3. We set upper bound of the term freqency in each document to 4 because we believe it is meanless that a word appears too many times in a single document. By doing this, we can prevent a document from creasing their ranking by delibrately putting to much unrelevant words in summary. 
4. Every time we expand the query by adding 2 words. We rearrange the order of words in new query according to the order of term frequence


=====================================================
f. Bing search account key
zawDQKZqojUuJJIbEYPbPXSLQkpa2eyJHI9zseVhXT8


