## Compliance Service


## Table of Contents <a name="TOC"></a>
* [Introduction](#Introduction)
* [API usage](#API-usage)
* [What is a LegalTag?](#What-is-a-LegalTag)
* [Ingestion workflow](#Ingestion-workflow)
* [Creating a LegalTag](#Creating-a-LegalTag)
* [LegalTag properties](#LegalTag-properties)
* [Creating a Record](#Creating-a-Record)
* [What are Derivatives?](#What-are-Derivatives)
* [Validating a LegalTag](#Validating-a-LegalTag)
* [Updating a LegalTag](#Updating-a-LegalTag)
* [Compliance on consumption](#Compliance-on-consumption)
* [The LegalTag Changed notification](#The-LegalTag-Changed-notification)
* [Permissions](#Permissions)
* [Version info endpoint](#version-info-endpoint)

## Introduction<a name="Introduction"></a>

This document covers how to remain compliant at the different stages of the data lifecycle inside the Data Ecosystem.
 
1. When ingesting data
2. Whilst the data is inside the Data Ecosystem 
3. When consuming data

The clients' interaction revolves around ingestion and consumption, so this is when you need to use what is contained in this guide. Point 2 should be mostly handled on the clients’ behalf; however, it is still important to understand that this is happening as it has ramifications on when and how data can be consumed.

Data compliance is largely governed through the Records in the storage service. Though there is an independent legal service and LegalTags entity, these offer no compliance by themselves.

Records have a Legal section in their schema and this is where the compliance is enforced. However, clients must still make sure they are using the Record service correctly to remain compliant.

Further details can be found in the [Creating a Record](#Creating-a-Record) section.

## API usage<a name="API-usage"></a>
Details of our APIs including how to create and retrieve LegalTags can be found [here.](https://community.opengroup.org/osdu/platform/security-and-compliance/legal/-/blob/5ecde60781c5ce0c92579e87c4b30fdf9219a9ab/docs/api/compliance_openapi.yaml)

##### Permissions

| **_API_** | **_Minimum Permissions Required_** |
| --- | --- |
| Access LegalTag APIs| users.datalake.viewers |
| Create a LegalTag | users.datalake.editors |
| Update a LegalTag | users.datalake.editors |

##### Headers

| **_Header_** | **_Description_** |
| --- | --- |
| data-partition-id (Required) | Specify the desired accessible partition id.|
| correlation-id (Optional) | Used to track a single request throughout all the services it passes through. This can be a GUID on the header with a key. If you are the service initiating the request, you should generate the id. Otherwise, you should just forward it on in the request. |

The Data Ecosystem stores data in different data partitions, depending on the access to those data partitions in the OSDU system. A user may have access to one or more data partitions.

[Back to table of contents](#TOC)

## What is a LegalTag?<a name="What-is-a-LegalTag"></a>
A LegalTag is the entity that represents the legal status of data in the Data Ecosystem. It is a collection of *properties* that governs how the data can be consumed and ingested. 

A legal tag is required for data ingestion. Therefore, creation of a legal tag is a necessary first step if there isn't a legal tag already exists for use with the ingested data. The LegalTag name needs to be assigned to the LegalTag during creation, and is used for reference. The name is the unique identifier for the LegalTag that is used to access it.

When data is ingested, it is assigned the LegalTag *name*. This name is checked for a corresponding valid LegalTag in the system.  A valid LegalTag means it exists and has not expired. If a LegalTag is invalid, the data is rejected. For instance, we may not allow ingestion of data from a certain country, or we may not allow consumption of data that has an expired contract.

In the same manner, the ingested data will be invalidated (soft-deleted) when the legal tag expires, as it would no longer be compliant.


## Ingestion workflow<a name="Ingestion-workflow"></a>

![API Security - High level](https://storage.googleapis.com/devportal-live-public/IngestionSequenceDiagram.png)

[](url)
The above diagram shows the typical sequence of events of a data ingestion. The important points to highlight are as follow:

* It is the ingestor's responsibility to create a LegalTag. LegalTag validation happens at this point.
* The Storage service validates the LegalTag for the data being ingested. 
* Only after validating a LegalTag exists can we ingest data.  No data should be stored at any point in the Data Ecosystem that does not have a valid LegalTag.

## Creating a LegalTag<a name="Creating-a-LegalTag"></a>
Any data being ingested needs a LegalTag associated with it.  You can create a LegalTag by using the POST LegalTag API e.g.

    POST /api/legal/v1/legaltags
    
<details><summary>Curl</summary>

```
curl --request POST \
  --url '/api/legal/v1/legaltags' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "name": "demo-legaltag",
        "description": "A legaltag used for demonstration purposes.",
        "properties": {
            "countryOfOrigin":["US"],
            "contractId":"No Contract Related",
            "expirationDate":"2099-01-01",
            "dataType":"Public Domain Data", 
            "originator":"OSDU",
            "securityClassification":"Public",
            "exportClassification":"EAR99",
            "personalData":"No Personal Data",
            "extensionProperties": {
                "anyCompanySpecificAttributes": "anyJsonTypeOfvalue"
            }
        }
}'
```

</details>
    
It is good practice for LegalTag names to be clear and descriptive of the properties it represents, so it would be easy to discover and to associate to the correct data with it. Also, the description field is a free form optional field to allow for you to add context to the LegalTag, making easier to understand and retrieve over time.

The "extensionProperties" field is an optional json object field and you may add any company specific attributes inside this field.

When creating LegalTags, the name is automatically prefixed with the data-partition-name that is assigned to the partition. So in the example above, if the given data-partition-name is **mypartition**, then the actual name of the LegalTag would be **mypartition-demo-legaltag**.

Valid values: The legalTag name needs to be between 3 and 100 characters and only alphanumeric characters and hyphens are allowed

To help with LegalTag creation, it is advised to use the Get LegalTag Properties API to obtain the allowed properties before creating a legal tag. This returns the allowed values for many of the LegalTag properties.

## LegalTag properties<a name="LegalTag-properties"></a>
Below are details of the properties you can supply when creating a LegalTag along with the values you can use. The allowed properties values can be data partition specific. Valid values associated with the property are shown. All values are mandatory unless otherwise stated.

You can get the data partition's specific allowed properties values by using LegalTag Properties API e.g.

    GET /api/legal/v1/legaltags:properties
    
<details><summary>Example 200 Response</summary>

```
    {
    	"countriesOfOrigin": {
    		"TT": "Trinidad and Tobago",
    		"TW": "Taiwan, Province of China",
    		"LR": "Liberia",
    		"DK": "Denmark",
    		"LT": "Lithuania",
    		"PY": "Paraguay",
    		"US": "United States",
    		...
    		...    		
    	},
    	"otherRelevantDataCountries": {
    		"PT": "Portugal",
    		"PW": "Palau",
    		"PY": "Paraguay",
    		"QA": "Qatar",
    		"AD": "Andorra",
    		"AE": "United Arab Emirates",
            ...
            ...
    	},
    	"securityClassifications": ["Private", "Public", "Confidential"],
    	"exportClassificationControlNumbers": ["No License Required", "Not - Technical Data", "EAR99", "0A998"],
    	"personalDataTypes": ["Personally Identifiable", "No Personal Data"]
    }
```

</details>  

<details><summary>Curl</summary>

```
curl --request GET \
  --url '/api/legal/v1/legaltags:properties' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
```

</details>

#### Country of origin
 Valid values: An array of ISO Alpha-2 country code. This is normally one value but can be more. This is required.
 
Notes: This is the country from where the data originally came, NOT from where the data was sent. The list of allowed countries is below. If ingesting Third Party Data, you can ingest data from any country that is not embargoed, if you have a valid contract associated with it that allows for this. This property is case sensitive.

#### Contract Id
 Valid values: This should be the Contract Id associated with the data or 'Unknown' or 'No Contract Related'.
 
 The contract ID must be between 3 and 40 characters and only include alphanumeric values and hyphens.
   
 Notes: This is always required for any data types. This property is case sensitive.
 
#### Expiration date
 Valid values: Any date in the future in the format yyyy-MM-dd (e.g. 2099-12-25) or empty.
 
 When the provided contract ID is "Unknown" or "No Contract Related", then the expiration date could be empty. However, if the date is provided, then it will be honored in validating the legal tag and the associated data, even when there's no contract is provided. As such, when the legal tag expires, the associated data will be soft-deleted from the Data Ecosystem.
 
 Notes: This sets the inclusive date when the LegalTag expires and the data it relates to is no longer usable in the Data Ecosystem. This normally is taken from the physical contracts expiration date i.e. when you supply a contract ID. This is non mandatory field, but is required for certain types of data e.g. 3rd party. If the field is not set it will be autopopulated with the value 9999-12-31. This property is case sensitive.
   
#### Originator
 Valid values: Should be the name of the client or supplier.
   
 Notes: This is always required. This property is case sensitive.
   
#### Data type

 | dataType | Data Residency Restriction |
| ----------- | -------- |
| "Public Domain Data" | "public data, no contract required" |
| "First Party Data" | "partition owner's data, no contract required" |
| "Second Party Data" | "client data, contract is required" |
| "Third Party Data" | "contract required" |
| "Transferred Data" | "EHC/Index data, no contract required" |

  
 Notes: Different data types are allowed dependent on the data partitions e.g. vendor partitions have different governing rules as opposed to standard partitions. To list the allowed data types for your data partition use the [LegalTag Properties](#LegalTag-properties). 
                                                                                                                        
#### Security classification
 Valid values: 'Public', 'Private', 'Confidential'
   
 Notes: This is the standard security classification for the data. We currently do not allow 'Secret' data to be stored in the Data Ecosystem. This property is NOT case sensitive.
   
#### Export classification
 Valid values: '0A998'(0 as Zero), 'EAR99', 'Not - Technical Data', 'No License Required'
   
 Notes: We currently only allow data with the ECCN classification 'EAR99' and '0A998'(0 as Zero). This property is NOT case sensitive.
   
#### Personal data
 Valid values: 'Personally Identifiable', 'No Personal Data'
   
 Notes: We do not currently allow data that is 'Sensitive Personal Information' and this should not be ingested. This property is NOT case sensitive.
 
 [Back to table of contents](#TOC)
  	 	
## Creating a Record<a name="Creating-a-Record"></a>
This relates to creating Records that are *NOT* derivatives. See the derivative section below for details on Record creation for derivative data.
 
Once you have a LegalTag created, you can assign it to as many Records as you like. However, it is the data managers' responsibility to assign accurate LegalTags to data.

When creating a Record, the following needs to be assigned for legal compliance:

* The LegalTag name associated with the Record
* The Alpha-2 country code of the original caller where the data is being ingested from 

Below is a full example of the payload needed when creating a Record. The *legal* section shows what is required.

<details><summary>Details</summary>

```
    [{
            "acl": {
                    "owners": [
                         "data.default.owners@{datapartition}.{domain}.org"
                    ],
                    "viewers": [
                        "data.default.viewers@{datapartition}.{domain}.org"
                    ]
            },
            "data": {
                    "count": 123456789
            },
            "id": "opendes:id:123456789",
            "kind": "opendes:welldb:wellbore:1.0.0",
            "legal" :{
                    "legaltags": [
                            "opendes-demo-legaltag"
                    ],
                    "otherRelevantDataCountries": ["US"] //the physical location of the person ingesting the data
            }
    }]
```

</details>    

* legaltags - This section represents the names of the LegalTag(s) associated with the Record. This has to be supplied when the Record represents raw or source data (i,e, not derivative data)
* otherRelevantDataCountries - This is the Alpha-2 country codes for the country the data was ingested from and the country where the data is located in Data Ecosystem. otherRelevantDataCountries is not part of the LegalTag(s). It is part of the legal property of the record. The location of the data center, in which the record is stored is automatically added to the otherRelevantDataCountries list when the record is created. This location depends on the environment/region that the partition locates. 

You can get the list of all valid LegalTags using the Get LegalTags API method. You can use this to help assign only valid LegalTags to data when ingesting.

    GET /api/legal/v1/legaltags?valid=true
    
<details><summary>Example 200 Response</summary>

```
    {
      "legalTags": [
        {
          "name": "osdu-thirdparty-public",
          "description": "",
          "properties": {
            "countryOfOrigin": [
              "US"
            ],
            "contractId": "A1234",
            "expirationDate": "2099-01-25",
            "originator": "OSDU",
            "dataType": "Third Party Data",
            "securityClassification": "Public",
            "personalData": "No Personal Data",
            "exportClassification": "EAR99"
          }
        },
        {
          "name": "osdu-welldb-public",
          "description": "",
          "properties": {
            "countryOfOrigin": [
              "US"
            ],
            "contractId": "AB123",
            "expirationDate": "2099-12-25",
            "originator": "OSDU",
            "dataType": "Public Domain Data",
            "securityClassification": "Public",
            "personalData": "No Personal Data",
            "exportClassification": "EAR99"
          }
        },
        ...
        ...
        ...
    }
```
    
</details>

<details><summary>Curl</summary>

```
curl --request GET \
  --url '/api/legal/v1/legaltags?valid=true' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
```

</details>

## What are Derivatives?<a name="What-are-Dervatives"></a>
Often when ingesting data into the Data Ecosystem, it is the raw data itself. In these scenarios, you associate a single LegalTag with this data.

However, in the case when the data to be ingested come from multiple sources, it is the case of derivative data. For instance, what if you take multiple Records from the Data Ecosystem and create a whole new Record based on them all? Or what if you run an algorithm over your seismic data and create an attribute associated with this data you want to ingest?

At this point, you have derivative data (i.e., data derived from data). In these scenarios, you will need to assign LegalTags to this new data which is the union of the LegalTags associated to all the source data from which it was created.

For instance, I have Data A associated with LegalTag 1, and Data B associated with LegalTag 2. If I create Data C from Data A and Data B, then I need to associate both LegaltTag 1 and LegalTag 2 to Data C.

### Creating derivative Records
When creating Records that represent derivative data, the following must be assigned:

* The Record Id and version of all the Records that are the direct parents of the new derivative. This is added to the *ancestry* section  
* The Alpha-2 country code of where the derivative was created

Below is an example of the minimum number of fields required to ingest a derivative Record.

<details><summary>Details</summary>

```
        [{
                "acl": {
                        "owners": [ 
                            "data.default.owners@{datapartition}.{domain}.org" 
                        ],
                        "viewers": [ 
                            "data.default.viewers@{datapartition}.{domain}.org"
                        ]
                },
                "data": {
                        "count": 123456789
                },
                "id": "opendes:id:123456789",
                "kind": "opendes:welldb:wellbore:1.0.0",
                "legal" :{
                        "otherRelevantDataCountries": ["US"] //the physical location of where the derivative was created
                },
                "ancestry" :{
                       "parents": ["opendes:id:1:version", "opendes:id:2:version"] //the record ids and versions of the Records this derivative was created from
                }    
        }]
```
</details>

As shown the parent Records are provided as well as the ORDC of where the derivative was created.  The Record service takes responsibility for populating the full LegalTag and ORDC values based on the parents.

[Back to table of contents](#TOC)

## Validating a LegalTag<a name="Validating-a-LegalTag"></a>

The Storage service validates whether a Record is legally compliant during ingestion and consumption. Therefore, you can delegate the effort to the Record service as the request will fail if the Record is not compliant.

However, there may be times you want to validate LegalTags directly. 

You can validate a LegalTag by using the LegalTag validate API supplying the names of the LegalTags you wish to validate  e.g.
 
    POST /api/legal/v1/legaltags:validate
    
<details><summary>Curl</summary>

```
curl --request POST \
  --url '/api/legal/v1/legaltags:validate' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "names": ["opendes-demo-legaltag"]
}'
```

</details>

If the LegalTag is valid, the response then looks something like this

<details><summary>Details</summary>

```    
    {
        "invalidLegalTags": [] 
    }
```

</details>    

If the LegalTag is invalid, the response then looks something like this

<details><summary>Details</summary>

```
    {
        "invalidLegalTags": [
            {"name":"opendes-demo-legaltag", "reason": "Contract expired"}
        ] 
    }
```

</details>

So if you just want to check that the given LegalTag(s) are currently valid, you only have to check if the returned 'invalidLegalTags' collection is empty.

Ingestion services forward the request to the LegalTag API using the same _SAuth_ token making the ingestion request. This checks both that a LegalTag exists and that the data has appropriate access to it.

## Updating a LegalTag<a name="Updating-a-LegalTag"></a>
One of the main cases where a LegalTag can become invalid is if a contract expiration date passes. This makes both the LegalTag invalid and *all* data associated with that LegalTag including derivatives.

In these situations we can update LegalTags to make them valid again and so make the associated data accessible. Currently we only allow the update of the *description*, *contract ID*, *expiration date* and *extensionProperties* properties. 

    PUT /api/legal/v1/legaltags

<details><summary>Curl</summary>

```
curl --request PUT \
  --url '/api/legal/v1/legaltags' \
  --header 'accept: application/json' \
  --header 'authorization: Bearer <JWT>' \
  --header 'content-type: application/json' \
  --header 'data-partition-id: opendes' \
  --data '{
        "name": "opendes-demo-legaltag",
        "contractId": "AE12345",
        "expirationDate": "2099-12-21",
        "extensionProperties": {
            "anyCompanySpecificAttributes": "anyJsonTypeOfvalue"
        }
    }
}'
```

</details>


*Note: There is a difference between "querying for valid or invalid LegalTag (List Legal Tag API )" and "checking if the legalTag is Valid (Validate Legal Tag API)".
The "List Legal tag" will check whether the tag is valid right now and "Validate Legal Tag" will check whether the tag would be valid after the next once-a-day update process.
Therefore, updating the LegalTag Expiration Date will not make the record visible as valid in the Valid Tag List until its status has been updated. 
The Update process validates the LegalTags and then updates the status of the LegalTag from Invalid to Valid or vice versa. This update process runs only once in a day.

[Back to table of contents](#TOC)
 
## Compliance on consumption<a name="Compliance-on-Consumption"></a>
As previously stated, the Records in the Storage service largely governs data compliance.  This means that if you use the Storage or Search core services, then compliance on consumption is handled on your behalf i.e. these services will not return Records that are no longer legally compliant.

However, there are use cases where you may not use these services all the time e.g. if you have your own operational data store.  In these cases you will need to check the LegalTags associated with your data are still valid before allowing consumption. For this, we have a PubSub topic that can be subscribed to.

*NOTE This topic can only be subscribed to if you deploy your service within the Data Ecosystem Google Project. 

This topic has the form

    projects/{googleProjectId}/topics/legaltags_changed	
    
This means you need to make a subscription to every data partition project you wish to receive the notifications on. 

*NOTE: When new data partitions are added into the Data Ecosystem, it may take up to 24 hours for the topic to become available to subscribe to.*

For more information on subscribing to PubSub topics, please use the Google documentation [here](https://cloud.google.com/pubsub/docs/subscriber).

## The LegalTag Changed notification<a name="The-LegalTag-Changed-notification"></a>
After subscribing to the topic, you will receive notifications daily. These notifications will list all LegalTags that have changed, and whether the LegalTag has become compliant or non-compliant.

<details><summary>Details</summary>

```
    {
        "statusChangedTags": [ { 
                "changedTagName": "legaltag-name1",
                "changedTagStatus": "compliant"
            },
            {
                "changedTagName": "legaltag-name2",
                "changedTagStatus": "incompliant"
            } ]
    }
```
</details>

The above shows an example message sent to subscribers. It shows you receive an array of items. Each item has the LegalTag name that has changed and whether it has changed to be compliant or incompliant.  

If it has become incompliant, you must make sure associated data is no longer allowed to be consumed.

If it is marked compliant, data that was not allowed for consumption can now be consumed through your services.

## The LegalTag About to Expire notification<a name="The-LegalTag-AboutToExpire-notification"></a>
After subscribing to the topic, you will receive notifications daily.
These notifications will list all LegalTags that will expire soon.
The definition of "soon" is configurable by the deployer.

This topic has the form

    projects/{googleProjectId}/topics/about-to-expire-legal-tag	

Note: this feature is also behind a feature flag, meaning the deployer
must specifically enable the feature.

<details><summary>Details</summary>

```
    {
        "aboutToExpireLegalTags": [ 
            {
                "dataPartitionId":"osdu",
                "tagName":"osdu-public-usa-dataset-osduonaws-test-about-to-expire",
                "expirationDate":"Mar 15, 2024"
            }
        ]
    }
```

</details>

Time to expire should be configurable on deployment level. We should be able to provide list of all time periods before LegalTag expires. This will be achieved by introducing new environment variable `legaltag.expirationAlerts`. Format is comma separated values which consists of number and letter suffix, for example: 3d,2w,1m
Where suffix letter should represent this time periods:

* #d - # number of days
* #w - # number of weeks
* #m - # number of months

[Back to table of contents](#TOC)

## Version info endpoint
For deployment available public `/info` endpoint, which provides build and git related information.
#### Example response:
```json
{
    "groupId": "org.opengroup.osdu",
    "artifactId": "storage-gcp",
    "version": "0.10.0-SNAPSHOT",
    "buildTime": "2021-07-09T14:29:51.584Z",
    "branch": "feature/GONRG-2681_Build_info",
    "commitId": "7777",
    "commitMessage": "Added copyright to version info properties file",
    "connectedOuterServices": [
      {
        "name": "elasticSearch",
        "version":"..."
      },
      {
        "name": "postgresSql",
        "version":"..."
      },
      {
        "name": "redis",
        "version":"..."
      }
    ]
}
```
This endpoint takes information from files, generated by `spring-boot-maven-plugin`,
`git-commit-id-plugin` plugins. Need to specify paths for generated files to matching
properties:
- `version.info.buildPropertiesPath`
- `version.info.gitPropertiesPath`

[Back to table of contents](#TOC)