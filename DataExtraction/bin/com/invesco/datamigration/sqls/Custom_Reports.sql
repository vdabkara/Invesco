<!-- 
	Adhoc Reports for following criteria:
A. Translated articles:
	a. Criteria:
               i. Version = French; Status = Live, with no corresponding Version = Canada; Status = Live
             ii. Version = Canada; Status = Live & IS_TRANSLATION=YES, with no corresponding Version = United States; Status = Live
             iii.Version = United States; Status = Live & IS_TRANSLATION=YES, with no corresponding Version = Canada; Status = Live

	b. Fields: (1) Master Locale, (2) Locale, (3) Doc ID, (4) Title, (5) Owner, (6) Status

-->

(first criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='fr_CA' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_CA')
 order by CHANNEL_REF_KEY ASC;

(second criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='en_CA' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_US') 
order by CHANNEL_REF_KEY ASC;

(third criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='en_US' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_CA') 
order by CHANNEL_REF_KEY ASC;

<!--
--------------------------------------------------------------------------------------------------	
	QUERY FOR GENERATING CATEGORIES REPORT

-->
SELECT A.CHANNEL_REF_KEY,A.DOCUMENT_ID,A.LOCALE,A.BASE_LOCALE,A.IS_TRANSLATION,B.DOCUMENT_STATUS,A.MAJOR_VERSION,A.MINOR_VERSION,
A.NAME,A.REFKEY,A.GUID,A.OBJECTID FROM category_details A, DOCUMENT_DETAILS B WHERE A.DOCUMENT_ID=B.DOCUMENT_ID AND A.LOCALE = B.LOCALE
AND A.MAJOR_VERSION=B.MAJOR_VERSION AND A.MINOR_VERSION=B.MINOR_VERSION;

<!--
--------------------------------------------------------------------------------------------------	
	QUERY FOR GENERATING VIEWS REPORT

-->
SELECT A.CHANNEL_REF_KEY,A.DOCUMENT_ID,A.LOCALE,A.BASE_LOCALE,A.IS_TRANSLATION,B.DOCUMENT_STATUS,A.MAJOR_VERSION,A.MINOR_VERSION,
A.NAME,A.REFKEY,A.GUID,A.OBJECTID FROM VIEW_details A, DOCUMENT_DETAILS B WHERE A.DOCUMENT_ID=B.DOCUMENT_ID AND A.LOCALE = B.LOCALE
AND A.MAJOR_VERSION=B.MAJOR_VERSION AND A.MINOR_VERSION=B.MINOR_VERSION