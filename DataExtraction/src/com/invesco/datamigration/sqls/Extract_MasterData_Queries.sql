/*
	USER MASTER DATA LOCALE WISE
*/
select a.*,b.localeid from 
INFOMANAGER.userinformation a, INFOMANAGER.userlocale b WHERE a.active='Y' and a.recordid=b.userid and 
b.localeid='en_CA';

/*
	REPLACEMENT TOKENS MASTER DATA
*/
SELECT a.localeid, a.lastmodifiedby, a.datemodified, a.tokenname,B.REPLACEMENTTEXT, b.replacementtext_non_formatted  FROM INFOMANAGER.replacementtokens A, 
INFOMANAGER.localizedtokens B WHERE a.recordid=b.tokenid;

/*
	USER ROLE MASTER DATA LOCALE WISE
*/
select a.*,b.localeid,b.name from INFOMANAGER.securityrole a,INFOMANAGER.securityroleresource b where 
a.recordid=b.roleid  and  b.localeid='en_CA';

/*
	USER ROLE MAPPING MASTER DATA
*/
select a.*,c.recordid as ROLE_RECORD_ID,C.SITEID AS ROLE_SITEID,C.REFERENCEKEY AS ROLE_REFERENCEKEY from  INFOMANAGER.userinformation a,INFOMANAGER.userrole b, INFOMANAGER.securityrole c where 
a.recordid=b.userid and b.roleid=c.recordid  and a.active='Y';

/*
	VIEWS MASTER DATA ALL LOCALES
*/
select a.recordid,a.referencekey,a.objectid,b.localeid,b.name from INFOMANAGER.site a, INFOMANAGER.siteresource b 
where a.recordid!='-1' and a.recordid=b.siteid;
