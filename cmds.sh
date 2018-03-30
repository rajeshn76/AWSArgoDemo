curl -H "Content-Type: application/json" -X POST -d '{
	"dataPath": "tweets/labeled-50.csv",
  	"featuresPath": "TweetsDataModel-direct.csv",
  	"columns": "tweets,afterRelease,hour,label",
  	"funcName": "net.mls.features.sentiment.ProcessTweetFn",
  	"modelPath": "model/lreg-tweet-direct.zip",
  	"modelJar": "jars/v2/model-serving.jar",
  	"kubeWfName": "model-endpoint-tweets-",
  	"dockerVersion": "v7"
}' http://localhost:8080/argo

curl -H "Content-Type: application/json" -X POST -d '{
	"dataPath": "app-reviews-partial.csv",
  	"featuresPath": "ReviewsDataModel-direct.csv",
  	"columns": "review,afterRelease,version,label",
  	"funcName": "net.mls.features.sentiment.ProcessReviewFn",
  	"modelPath": "model/lreg-review-direct.zip",
  	"modelJar": "jars/v2/model-serving.jar",
  	"kubeWfName": "model-endpoint-review-",
  	"dockerVersion": "v1"
}' http://localhost:8080/argo