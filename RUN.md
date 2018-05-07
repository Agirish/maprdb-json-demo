source ~/.bash_profile > /dev/null 2>&1

mkdir -p ~/drillAutomation/ > /dev/null 2>&1
cd ~/drillAutomation/ > /dev/null 2>&1

rm -Rf ojai-tests > /dev/null 2>&1
git clone git@github.com:Agirish/maprdb-json-demo.git ojai-tests > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error cloning repository"
  exit -1
fi
cd ojai-tests > /dev/null 2>&1
mvn clean install > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error building tests"
  exit -1
fi

hadoop fs -rm -r /drill/testdata/ojai/ > /dev/null 2>&1
hadoop fs -mkdir -p /drill/testdata/ojai > /dev/null 2>&1
hadoop fs -copyFromLocal /home/MAPRTECH/qa/dataset/yelp/yelp_academic_dataset_business.json /drill/testdata/ojai/business.json > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error copying data"
  exit -1
fi

mapr importJSON  -idField business_id -mapreduce false -src /drill/testdata/ojai/business.json -dst /drill/testdata/ojai/business_table > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error importing data"
  exit -1
fi

java -cp .:target/*  com.mapr.demo.FindQueryDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in FindQueryDemo"
  exit -1
fi
java -cp .:target/*  com.mapr.demo.LimitAndOffsetDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in LimitAndOffsetDemo"
  exit -1
fi
java -cp .:target/*  com.mapr.demo.LimitAndOffsetJsonDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in LimitAndOffsetJsonDemo"
  exit -1
fi
java -cp .:target/*  com.mapr.demo.OrderByQueryDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in OrderByQueryDemo"
  exit -1
fi
java -cp .:target/*  com.mapr.demo.QueryDebuggingDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in QueryDebuggingDemo"
  exit -1
fi
java -cp .:target/*  com.mapr.demo.QueryOptionsDemo > /dev/null 2>&1
if [ $? != 0 ]
then
  echo "Error running tests in QueryOptionsDemo"
  exit -1
fi

echo "All tests completed successfully"
