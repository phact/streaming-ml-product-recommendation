+++
date = "2017-04-10T14:21:00-05:00"
title = "Power Tools ML Streaming Product Recommendations"
weight = 1
+++

This is a guide for how to use the power tools machine learning streaming product recommendation asset brought to you by the Vanguard team.

### Motivation

Machine learning powered recommendation engines have wide applications across multiple industries as companies seeking to provide their end customers with deep insights by leveraging data in the moment. Although there are many tools that allow for historical analysis that yield recommendations, DataStax Enterprise (DSE) is particularly well suited to power real-time recommendation / personalization systems. It is when it comes to operationalizing and productionizing analyitical systems that DSE will prove most useful. This is largely due to DSEs design objectives of operating at scale, in a distributed fashion, and while fulfilling performance and availability requirements required for user facing, mission critical applications.

### What is included?

This field asset includes a working application for real-time recommendations leveraging the following DSE functionality:

* Machine Learning
* Streaming analytics
* Batch analytics
* Real-time JDBC / SQL (dynamic caching)
* DSEFS

### Business Take Aways

By streaming customer market basket data from a retail organization through DSE analytics and using it to train a Collaborative Filterning Machine Learning model, we are able to maintain a top K list of recommended products by customer that reflect their historical and recent buying patterns.

In the retail industry, both online and brick and mortar businesses are leveraging ML and real-time analytics pipelines to gather insights that become differentiators for them in the marketplace. The DataStax stack is the foundation for enterprise personalization / recommendation systems across multiple industries.

### Technical Take Aways

For a technical deep dive, take a look at the following sections:

- Machine learning model
- Streaming analytics pipeline
- Real-time JDBC / SQL (dynamic caching)
