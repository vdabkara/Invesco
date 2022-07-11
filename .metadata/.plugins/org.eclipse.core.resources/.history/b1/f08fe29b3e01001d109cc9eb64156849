<!-- 
	Adhoc Reports for following criteria:
A. Translated articles:
	a. Criteria:
               i. Version = French; Status = Live, with no corresponding Version = Canada; Status = Live
             ii. Version = Canada; Status = Live, with no corresponding Version = United States; Status = Live
             iii.Version = United States; Status = Live, with no corresponding Version = Canada; Status = Live

	b. Fields: (1) Master Locale, (2) Locale, (3) Doc ID, (4) Title, (5) Owner, (6) Status

-->

(first criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='fr_CA' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_CA') order by CHANNEL_REF_KEY ASC;

(second criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='en_CA' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_US') order by CHANNEL_REF_KEY ASC;

(third criteria)
select BASE_LOCALE,LOCALE,DOCUMENT_ID,TITLE,OWNER_NAME,DOCUMENT_STATUS from DOCUMENT_DETAILS where DOCUMENT_STATUS='LIVE' AND 
LOCALE='en_US' and  DOCUMENT_ID not in (SELECT DOCUMENT_ID FROM DOCUMENT_DETAILS WHERE DOCUMENT_STATUS='LIVE' AND LOCALE='en_CA') order by CHANNEL_REF_KEY ASC;
