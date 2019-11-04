%/cu/underwrite/
&/tmp/

@application####                       isam
csc/application####
	medical						text			1	;	Y:ตรวจสุขภาพ N:ไม่ตรวจสุขภาพ
    referenceNo                	text        	5   ;	running
	tempPolicyNo				text			8	
    preName                    	text       		20  ;	Thai Pre-Name
    firstName                  	text       		30  ;	Thai First Name
    lastName                   	text       		30  ;	Thai Last Name
    oldPreName                 	text       		20  ;	คำนำหน้าชื่อเดิม
    oldFirstName               	text       		30  ;	ชื่อเดิม
    oldLastName                	text      	 	30  ;	นามสกุลเดิม
    sex                        	text        	1
    birthDate                  	text        	8   ;	Birthdate
    insuredAge                 	number      	2   ;	Age of Insured
    race                       	text       		20 	;	เชื้อชาติ
    nationality                	text       		20  ;	สัญชาติ
    religious                  	text       		6  	;	ศาสนา
	cardType					text			1	; 	1:idNo 2:ข้าราชการ 3:ต่างด้าว 4:หนังสือเดินทาง 5:ทะเบี้ยบ้าน 6:อื่น
	cardOther					text			20	;	ชื่อบัตรอืนๆ
    idNo                       	text       		13  ;	Id of Insured
    idExpiryDate               	text        	8   ;	Id Expiry Date
    idIssued                   	text       		20  ;	Place that Issued Id Card
    province                 	text       		20  ;	
	amphur						text			20	; 	
	country						text			3	;
    expiryDate                 	text        	4   ;	YYMM (A.D.)
	contactAddress				text			1	;	1:contact 2:domicile 3:office	
    height                     	number      	4  1
    weight                     	number      	4  1
    marriageStatus             	text        	1   ;	1: Single 	4: Divorce 	3: Widowhood  2: Marriage
	marriageName				text			90	;	ชื่อสามี/ภรรยา
    planCode                   	text        	4   ;	Plan Code
    planPA                   	text        	1   ;	PAPlan 
    mode                       	text        	1   ;	
    rpNo                       	text       		12  ;	Receipt No.
    paymentType                	text        	1   ;	utility.branch.PayBy
    effectiveDate              	text        	8   ;	Effective Date
    lifeSum                    	number      	9   ;		Life Sum
    lifePremium                	number      	9   ;	Life Premium
    riderPremium               	number      	9   ;	rider Premium
	channelCode					text			3	;  	000:TL
    branch	                	text        	3   ;	
	userID						text			7
	saleID						text			10	
	time						text			6
    sysDate                    	text       	 	8   ;	The Date that Insured Apply
    printDate                  	text       		8   ;	Date for Print Receipt
    approve                    	text        	1   ;	A: Accept     R: Reject
  	status                     	text        	1   ;	N: New Case ;	M: Update in Master
(referenceNo)
(tempPolicyNo)                 	dup mod
(branch sysDate)				dup
(lastName firstName)			dup mod
(idNo)							dup mod

@occupation####                       isam
csc/occupation####
    referenceNo                	text        	5	;	running
	sequence				   	number			1	
    occupationType             	text        	1   ;	Occupation Type
    occupationCode             	number      	2   ;	Code Detail Occupation
    jobPosition                	text       		60  ;	Job Position
    income                     	number      	9   ;	INCOME PER YEAR
    workType                   	text       		30  ;	ลักษณะงานที่ทำ
    businessType               	text       		30  ;	ลักษณะธุรกิจ
(referenceNo sequence)

@rider####                             isam
csc/rider####
    referenceNo                	text        	5    ;	running
    riderType                  	text        	3    ;	Rider Type
    riderSum                   	number      	9    ;	Rider Sum
    riderPremium               	number      	9    ;	Rider Premium
    riderStatus                	text        	1    ;	Rider Status
(referenceNo riderType)

@payer####                             isam
csc/payer####
    referenceNo                	text        	5    ;	running
    preName                    	text       		30
    firstName                  	text       		30
    lastName                   	text       		30
    payerType                  	text        	1    ;1:จ่ายเอง 2:จ่ายแทน
    relationship               	text       		30   ;ความสัมพันธ์กับผู้เอาประกัน
(referenceNo)

@address####                           isam
csc/address####
    referenceNo                	text        	5    ;	running
    addressType                	text        	1    ;1 : contact 2 : domicile 3:office
    address                    	text      		128
    tumbon                     	text        	5    ;ตำบล
    zipCode                    	text        	5    ;รหัสไปรษณีย์
(referenceNo addressType)

@beneficiary####                       isam
csc/beneficiary####
    referenceNo                	text        	5    ;	running
    sequence                   	number      	1
    preName                    	text       		30
    firstName                  	text       		30
    lastName                   	text       		30
    relationshipCode           	text        	2
    percentShare               	number      	5  2
    age                        	number      	2
    birthDate                  	text       	 	8
	phoneNo						text			10
	mobilePhone					text			10
(referenceNo sequence)

@beneficiaryaddress####                    isam
csc/beneficiaryaddress####
    referenceNo                	text        	5    ;	running
    seq                        	text        	1
    address                    	text      		128
    tumbon                     	text        	5
    zipCode                    	text        	5
    email                      	text       		35
(referenceNo seq)

@medical####                           isam
csc/medical####
    referenceNo                	text        	5    ;	running
    choice                     	number      2
    seqNo                      	text        2
    medOk                      	text        1    ; Y:ไม่เคย   N:เคย
    medical                    	text       20    
    detail1                    	text       50
    detail2                    	text       50
    detail3                    	text       50
    detail4                    	text       50
    detail5                    	text       50
    detail6                    	text       50
(referenceNo choice seqNo)

@email####                             isam
csc/email####
    referenceNo                text        	5    ;	running
    sequence                   text        1
    email                      text       40
(referenceNo sequence)


@phone####                             isam
csc/phone####
    referenceNo                	text        	5    ;	running
    type                       text        1    ;H:HomePhone O:OfficePhone M:Mobile F:Fax  H+O = "T" For Master
    sequence                   text        1
    phoneNo                    text       30
    extension                  text       30
(referenceNo type sequence)

@guardian####                          isam
csc/guardian####
    referenceNo                text        6    ; running
    preName                    text       30    ;เลขอ้างอิงชื่อผู้ปกครอง
                                                ;(จาก 20->30 ตาม reformat จาก 192.1.2.8 12/06/51)
    firstName                  text       30
    lastName                   text       30
    birthDate                  text        8
    parentAge                  text        2    ;อายุผู้ปกครองa
    idNo                       text       13
    idExpiryDate               text        8
    idIssued                   text       20
    idProvince                 text       20    ;Id Province
    parentSex                  text        1
    marriageStatus             text        1    ;1: Single
                                                ;2: Divorce
                                                ;3: Widowhood
                                                ;4: Marriage
    occupationType             text        1    ;Occupation Type
    occupationCode             number      2    ;Code Detail Occupation
    occupation                 text       30    ;occupation
    jobPosition                text       60    ;Job Position
    parentClass                text        1
    hivFlag                    text        1    ;'Y':ผู้ปกครองตรวจ HIV  'N' : ผู้ปกครองไม่ตรวจ HIV (from newcase from 48)
    spPreName                  text       30    ;Thai Pre-Name
    spFirstName                text       30    ;Thai First Name
    spLastName                 text       30    ;Thai Last Name
    spOccupationType           text        1    ;Occupation Type
    spOccupationCode           number      2    ;Code Detail Occupation
    spOccupation               text       30    ;occupation
    spJobPosition              text       60    ;Job Position
    relationshipCode           text        2
    reserve                    text        1
(referenceNo)                mod

@referenceno							isam
csc/referenceno
	yymm						text			4	 
    referenceNo                	text        	5    ;	running
(yymm)
