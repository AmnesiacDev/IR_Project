This App is a Search Engine Implementation

It takes a user's Query (Search phrase) and then the crawler look on wikipedia for documents
The crawler starts on a preset seed
The App uses TF-IDF scores on the collected documents and the Query
Cosine Similarity is used to order documents by relevence to user's query


To access the app go to -> IR_Project->src->main/java/com/miyuki-> JWEC.java and App.java

-WikiCrawler Class is the crawler which takes a preset seed and recursively Crawl on Wikipedia

-TFIDFCalculate Calculates the TF-IDF of the documents collected

-QueryRanker Calculates TF-IDF of the tokenized user's search query and then calculates cosine similarity between query and documents and ranks them in descending order
