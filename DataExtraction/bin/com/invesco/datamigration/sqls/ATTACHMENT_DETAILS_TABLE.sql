CREATE TABLE TABLE_NAME_PLACEHOLDER (
	[ID] [int] IDENTITY(1,1) NOT NULL,
	[CHANNEL_NAME] [varchar](200) NULL,
	[CHANNEL_REF_KEY] [varchar](200) NULL,
	[DOCUMENT_ID] [varchar](100) NULL,
	[LOCALE] [varchar](20) NULL,
	[BASE_LOCALE] [varchar](20) NULL,
	[IS_TRANSLATION] [varchar](20) NULL,
	[MAJOR_VERSION] [varchar](50) NULL,
	[MINOR_VERSION] [varchar](50) NULL,
	[SOURCE_FILE_NAME] [varchar](1000) NULL,
	[SOURCE_FILE_PATH] [varchar](2000) NULL,
	[DEST_FILE_NAME] [varchar](1000) NULL,
	[DEST_FILE_PATH] [varchar](2000) NULL,
	[PROCESSING_STATUS] [varchar](100) NULL,
	[ERROR_MESSAGE] [varchar](4000) NULL,
	[REC_CREATION_TMSTP] [datetime] NULL,
	[REC_MODIFIED_TMSPT] [datetime] NULL,
	CONSTRAINT [PK_TABLE_NAME_PLACEHOLDER] PRIMARY KEY CLUSTERED 
  	(
		[ID] ASC
	)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]  	
