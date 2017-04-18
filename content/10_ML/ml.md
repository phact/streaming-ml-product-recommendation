+++
date = "2017-04-10T14:21:00-05:00"
title = "Machine Learning"
weight = 1 
+++

DSE Analytics comes equipped with Machine Learning capabilities as part of its distributed compute framework. The algorithms available fall into the following groups:

- Basic statistics
- Classification and regression
- Collaborative filtering
- Clustering
- Dimensionality reduction
- Feature extraction and transformation
- Frequent pattern mining
- Evaluation metrics
- PMML model export
- Optimization

The algorithms in these categories (which ship as part of the SparkML package) are full featured, easy to use and configure, and scalable. Some of the algorithms can run in both Batch mode and Streaming mode; operating on RDDs and DStreams respectively.

Users should carefuly select the algorithms that will be relevant for the particular use case they are adressing. In some cases, more than one algorithm can be used in a single analysis.

### Recommendation / Personalization

In this use case we focus on a class of algorithms known as Collaborative Filtering and more specifically, we leverage Alternating Least Squares (ALS), due to the large data volumes and preformance requirements of of real-time operational recommendation systems.

####Collaborative Filtering 

Common ML technique for recommendation engines. Underlying assumption is that similar people have similar tastes so you can predict a user's tastes (and offer him or her a recommendation) based on what similar people like. This is as opposed to a Content based approach in which a profile is built for each user and for each item based on their traits and characteristics, i.e. feature lists in the case of items or preference surveys in the case of users. Collaborative filtering allows us to obtain good recommendation results without the additional data collection required for content based recommendations. It also has the added advantage of being domain free making it applicable and interesting to multiple industries.


####Downsides of Collaborative Filtering
Cold start problem - new items are hard since there's no experience to base them off of. Content based strategies are better in this case.

####Implicit vs. Explicit Data

Implicit means we do not have direct information about the user's preferences with regards to an item.
 - we do not have information about the items a user dislikes (Note: this is common when we have a buy dataset like the retail example we leverage today)
 - there is inherent noise in implicit datasets (i.e. user watched a movie but didn't like it, the TV was on but the user was asleep, use purchased a product but it was a gift, etc.)
 - we refer to the value in the implicit dataset as confidence and are careful to keep in mind that it only correlates with preference but does not always equal or imply preference

On the other hand, explicit datasets are specific abut the user's preference with regards to an item.
 - explicit data may take the form of dislikes (i.e. in a star based rating system, one star is bad)
 - explicit datasets are less noisy than implicit datasets
 - the value in an explicit dataset can be refered to as preference

For the purposes of this field asset we focus on implicit datasets for Colaborative filtering. Deducing preferences from implicit datasets has the following difficulties that we should keep in mind.

* adjusting for supply is difficult. i.e. if there are not many purchases of a certain item it may a result of limited supply rather than lack of popularity or prefference.
* because the data we use for training is implicit, it is difficult to measure or quantify the success of the model (be careful of overfitting)

###Alternating Least Squares (ALS) and Some Math

The simplest methods for Collaborative Filtering are neighborhood models which find related products by identifying similar users and recommending each other's items or look at related items and recommend them to users that have purchased them. On the other end of the spectrum, compute heavy latent factor models can produce good results, though they tend to be too expensive to compute at scale in near real time.

The Alternating Least Squares method falls in the middle, powerful enough to provide good results but simple enough both to reason about and to perform at scale in near real time.

Some math:

    Observations --> r(u, i), confidence (number of picks) of item i by user u

Note: observations will have much more coverage for implicit than explicit (ratings are less common than purchases). With our implicit dataset, we should have data about every available item.

    Preference --> p(u, i), comes from binarising r(u, i). if r > 0 --> 1  if r=0 --> 0

    Confidence --> c(u, i) = 1 + alpha * r(u, i) ; per the paper alpha 40 is a good choice (empirically)

ALS uses Latent factors to narrow down relationships between items and users. We break down the preference into two factors X and Y which will be our latent factors for users and items respectively:

p(u, i) = x(u) * y(i)

In the ALS paper you can read about the cost function that is used to solve for x and y. It is a bit hairy but TL;DR, by minimizing the cost function (this is optimization) we are able to find the values for our latent factors that will give us the most accurate results.

Seb to add image here

The most popular theory for this kind of optimization in the realms of ML and AI is Linear Gradient Decent, however, it is too slow to operate at the scale required. ALS fix one of the two factors at a time creating a quadradic equation which can be solved using calculus and repeat using the derived factor as the new fixed value. Each step is guaranteed to get us closer to the optimal value and we keep going back and forth until we are close enough, hence the Alternating in ALS. The math is hairy but the key thing to note is  that the whole process will scale linearly with the size of the data in the training set.

In the end we recommend to user u the K available items with the largest value of p:

    p(u, i) = x(u) * y(i)

> In addition, we offer a novel way to give explanations to recommendations given by this factor model.

You can derive that preference p is equal to the sum product of the similarities and the confidence vertices, where similarity is computed through latent factor optimization.

In the end this looks a lot like item-oriented neighborhood models, which are easier to reason around and explain. We can also see alternating least squares as a powerful preprocessor for a neighborhood based method, where item similarities are learned through a principled optimization process.

###Related Links / Bibliography

Comcast talk (slides):
https://spark-summit.org/2015-east/wp-content/uploads/2015/03/SSE15-18-Neumann-Alla.pdf

Comcast talk (video):
https://www.youtube.com/watch?v=cg8lm7ANxkA&index=4&list=PL-x35fyliRwiiYSXHyI61RXdHlYR3QjZ1

Alternatig Least Squares (original paper)
http://yifanhu.net/PUB/cf.pdf 

Streaming ALS:
https://github.com/brkyvz/streaming-matrix-factorization
