package com.vunity.family

data class FamilyDto(
    var contentFound: Boolean?,
    var `data`: FamilyData?,
    var message: String?,
    var status: Int?
)

data class FamilyData(
    var __v: Int?,
    var _id: String?,
    var familyInfo: FamilyInfo?,
    var familyTree: List<FamilyTreeData>?,
    var personalInfo: PersonalInfo?,
    var shraardhaInfo: ShraardhaInfo?,
    var user_id: String?
)

data class FamilyInfo(
    var gothram: Any?,
    var kulatheivam: Any?,
    var madhava: Any?,
    var motherTongue: Any?,
    var nativity: Any?,
    var panchangam: Any?,
    var pondugalName: Any?,
    var poojas: MutableList<Any>?,
    var pravara: Any?,
    var rushi: Any?,
    var sampradhayam: Any?,
    var smarthaSubsect: Any?,
    var smarthaSubsectTelugu: Any?,
    var soothram: Any?,
    var thilakam: Any?,
    var vaishnavam: Any?,
    var vaishnavamTelugu: Any?,
    var vedham: Any?
)

data class FamilyTreeData(
    var _id: String,
    var city: String,
    var dateOfBirth: String,
    var mobileNumber: String,
    var nakshathram: String,
    var name: String,
    var padham: String,
    var rashi: String,
    var relationship: String
)

data class PersonalInfo(
    var city: String?,
    var dateOfBirth: String?,
    var email: String?,
    var gender: String?,
    var maritalStatus: String?,
    var mobileNumber: String?,
    var nakshathram: String?,
    var name: String?,
    var padham: String?,
    var placeOfBirth: String?,
    var rashi: String?,
    var sharma: String?,
    var timeOfBirth: String?
)

data class ShraardhaInfo(
    var gothram: Gothram?,
    var name: Name?,
    var samayal: Samayal?,
    var shraddha_vazhakkam: Vazhakkam?,
    var thithi: List<Thithi>?
)

data class Gothram(
    var mathruGothram: Any?,
    var pithruGothram: Any?
)

data class Name(
    var mathamaha: Any?,
    var mathamahi: Any?,
    var mathru: Any?,
    var mathruPithamaha: Any?,
    var mathruPithamahi: Any?,
    var mathruPrapitamahi: Any?,
    var mathruPrapithamaha: Any?,
    var pithamaha: Any?,
    var pithamahi: Any?,
    var pithru: Any?,
    var prapithamaha: Any?,
    var prapithamahi: Any?
)

data class Samayal(
    var bhakshanam: MutableList<Any>?,
    var kari: MutableList<Any>?,
    var morkuzhambu: Any?,
    var other: Any?,
    var payasam: Any?,
    var pazhanga: MutableList<Any>?,
    var poruchchakuttu: Any?,
    var puliKuttu: Any?,
    var rasam: Any?,
    var samayalType: Any?,
    var sweetPachchadi: Any?,
    var thugayal: MutableList<Any>?,
    var thyirPachchadi: Any?,
    var uruga: MutableList<Any>?
)

data class Vazhakkam(
    var koorcham: Any?,
    var krusaram: Any?,
    var pindamCount: Any?,
    var pundraDharanam: Any?,
    var tharpanaKoorcham: Any?,
    var other: Any?
)

data class Thithi(
    var _id: String?,
    var date: String?,
    var masamChandramanam: String?,
    var masamSauramanam: String?,
    var name: String?,
    var paksham: String?,
    var relationship: String?,
    var thithi: String?,
    var time: String?
)

data class FamilyBody(
    var city: String?,
    var dateOfBirth: String?,
    var mobileNumber: String?,
    var nakshathram: String?,
    var name: String?,
    var padham: String?,
    var rashi: String?,
    var relationship: String?
)

data class ThithiData(
    var date: String,
    var masamChandramanam: String,
    var masamSauramanam: String,
    var name: String,
    var paksham: String,
    var relationship: String,
    var thithi: String,
    var time: String
)







