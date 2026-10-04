package com.example.data.local

import com.example.data.model.*

object InitialData {

    fun getInitialPatients(): List<Patient> = listOf(
        Patient(
            id = "pat_001",
            name = "Manuel Domingos Kiala",
            biNumber = "004829124LA042",
            phone = "+244 923 456 789",
            age = 34,
            gender = "Masculino",
            coverageType = "Particular",
            status = PatientStatus.WAITING_TRIAGE,
            chiefComplaint = "Febre alta vespertina, calafrios e cefaleia há 2 dias. Suspeita de malária.",
            createdAt = System.currentTimeMillis() - 45 * 60 * 1000
        ),
        Patient(
            id = "pat_002",
            name = "Esperança Sebastião de Oliveira",
            biNumber = "007231456BE038",
            phone = "+244 934 112 334",
            age = 28,
            gender = "Feminino",
            coverageType = "ENSA Seguros",
            status = PatientStatus.WAITING_CONSULTATION,
            chiefComplaint = "Dor no corpo, cansaço extremo e sensação de febre com náuseas.",
            triage = Triage(
                patientId = "pat_002",
                systolicBP = 120,
                diastolicBP = 80,
                temperatureCelsius = 38.6,
                weightKg = 62.0,
                heightCm = 164.0,
                bloodGlucoseMgDl = 98,
                heartRateBpm = 86,
                painScale = 6,
                nurseNotes = "Febre axilar confirmada (38.6°C). Pele quente. Pulso rítmico. Glicemia dentro da normalidade.",
                recordedBy = "Enf. Carlos Benguela",
                timestamp = System.currentTimeMillis() - 25 * 60 * 1000
            ),
            createdAt = System.currentTimeMillis() - 60 * 60 * 1000
        ),
        Patient(
            id = "pat_003",
            name = "António Muondo Kwanza",
            biNumber = "001928374HA019",
            phone = "+244 912 887 766",
            age = 46,
            gender = "Masculino",
            coverageType = "Particular",
            status = PatientStatus.WAITING_CONSULTATION,
            chiefComplaint = "Tonturas ao levantar, aperto na nuca e cansaço visual.",
            triage = Triage(
                patientId = "pat_003",
                systolicBP = 150,
                diastolicBP = 95,
                temperatureCelsius = 36.8,
                weightKg = 86.0,
                heightCm = 175.0,
                bloodGlucoseMgDl = 142,
                heartRateBpm = 82,
                painScale = 4,
                nurseNotes = "Pressão arterial elevada (150/95 mmHg - Hipertensão Estágio 1). Orientado a repouso pré-consulta.",
                recordedBy = "Enf. Carlos Benguela",
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000
            ),
            createdAt = System.currentTimeMillis() - 75 * 60 * 1000
        ),
        Patient(
            id = "pat_004",
            name = "Teresa Rosa da Silva",
            biNumber = "008912384LA091",
            phone = "+244 945 990 011",
            age = 22,
            gender = "Feminino",
            coverageType = "Victoria Seguros",
            status = PatientStatus.IN_CONSULTATION,
            chiefComplaint = "Tosse produtiva, odinofagia e coriza há 4 dias.",
            triage = Triage(
                patientId = "pat_004",
                systolicBP = 115,
                diastolicBP = 75,
                temperatureCelsius = 37.4,
                weightKg = 54.0,
                heightCm = 160.0,
                bloodGlucoseMgDl = 92,
                heartRateBpm = 76,
                painScale = 3,
                nurseNotes = "Subfebril. Murmúrio vesicular presente bilateralmente.",
                recordedBy = "Enf. Carlos Benguela",
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000
            ),
            createdAt = System.currentTimeMillis() - 90 * 60 * 1000
        ),
        Patient(
            id = "pat_005",
            name = "João Baptista Afonso",
            biNumber = "003847291LN054",
            phone = "+244 928 334 556",
            age = 52,
            gender = "Masculino",
            coverageType = "Nossa Seguros",
            status = PatientStatus.COMPLETED,
            chiefComplaint = "Consulta de rotina, controlo metabólico e renovação de medicação.",
            triage = Triage(
                patientId = "pat_005",
                systolicBP = 125,
                diastolicBP = 82,
                temperatureCelsius = 36.6,
                weightKg = 78.0,
                heightCm = 172.0,
                bloodGlucoseMgDl = 110,
                heartRateBpm = 72,
                painScale = 0,
                nurseNotes = "Sinais vitais normais e estáveis.",
                recordedBy = "Enf. Carlos Benguela",
                timestamp = System.currentTimeMillis() - 120 * 60 * 1000
            ),
            createdAt = System.currentTimeMillis() - 140 * 60 * 1000
        )
    )

    fun getInitialMedications(): List<Medication> = listOf(
        Medication(
            id = "med_001",
            tradeName = "Coartem 80/480mg",
            genericName = "Arteméter + Lumefantrina",
            dosageForm = "Comprimidos (Cx com 6 comp.)",
            batchNumber = "CRT-2025A",
            expirationDate = "15/12/2026",
            daysUntilExpiry = 72,
            stockQuantity = 45,
            minStockThreshold = 10,
            unitPriceKz = 4800.0,
            category = "Antimaláricos",
            barcode = "560123490011"
        ),
        Medication(
            id = "med_002",
            tradeName = "Paracetamol 500mg",
            genericName = "Paracetamol",
            dosageForm = "Comprimidos (Cx com 20 comp.)",
            batchNumber = "PCT-8841",
            expirationDate = "24/10/2026",
            daysUntilExpiry = 20, // Dispara alerta urgente de validade (< 30 dias)
            stockQuantity = 8, // Dispara alerta de estoque baixo (< 10)
            minStockThreshold = 10,
            unitPriceKz = 1200.0,
            category = "Analgésicos & Antipiréticos",
            barcode = "560123490028"
        ),
        Medication(
            id = "med_003",
            tradeName = "Amoxicilina + Clavulanato 500/125mg",
            genericName = "Amoxicilina + Ácido Clavulânico",
            dosageForm = "Comprimidos Revestidos (Cx com 16 comp.)",
            batchNumber = "AMX-9012",
            expirationDate = "18/08/2027",
            daysUntilExpiry = 320,
            stockQuantity = 32,
            minStockThreshold = 10,
            unitPriceKz = 6500.0,
            category = "Antibióticos",
            barcode = "560123490035"
        ),
        Medication(
            id = "med_004",
            tradeName = "Omeprazol 20mg",
            genericName = "Omeprazol Cápsulas",
            dosageForm = "Cápsulas Gastrorresistentes (Cx com 28 cáp.)",
            batchNumber = "OMP-4412",
            expirationDate = "05/04/2027",
            daysUntilExpiry = 180,
            stockQuantity = 50,
            minStockThreshold = 10,
            unitPriceKz = 2500.0,
            category = "Gastrointestinais",
            barcode = "560123490042"
        ),
        Medication(
            id = "med_005",
            tradeName = "Diclofenac 50mg",
            genericName = "Diclofenac de Sódio",
            dosageForm = "Comprimidos (Cx com 20 comp.)",
            batchNumber = "DCF-7718",
            expirationDate = "12/11/2026",
            daysUntilExpiry = 39,
            stockQuantity = 6, // Alerta de Estoque Baixo!
            minStockThreshold = 10,
            unitPriceKz = 1800.0,
            category = "Anti-inflamatórios",
            barcode = "560123490059"
        ),
        Medication(
            id = "med_006",
            tradeName = "Ciprofloxacina 500mg",
            genericName = "Cloridrato de Ciprofloxacina",
            dosageForm = "Comprimidos (Cx com 10 comp.)",
            batchNumber = "CPR-6612",
            expirationDate = "30/09/2027",
            daysUntilExpiry = 360,
            stockQuantity = 25,
            minStockThreshold = 10,
            unitPriceKz = 3900.0,
            category = "Antibióticos",
            barcode = "560123490066"
        ),
        Medication(
            id = "med_007",
            tradeName = "Ibuprofeno 400mg",
            genericName = "Ibuprofeno",
            dosageForm = "Comprimidos (Cx com 30 comp.)",
            batchNumber = "IBP-3391",
            expirationDate = "14/06/2027",
            daysUntilExpiry = 250,
            stockQuantity = 40,
            minStockThreshold = 10,
            unitPriceKz = 2100.0,
            category = "Anti-inflamatórios",
            barcode = "560123490073"
        ),
        Medication(
            id = "med_008",
            tradeName = "Sais de Reidratação Oral (SRO)",
            genericName = "Cloreto de Sódio + Glicose + Citrato",
            dosageForm = "Saquetas para diluição em 1 Litro",
            batchNumber = "SRO-1102",
            expirationDate = "20/03/2028",
            daysUntilExpiry = 530,
            stockQuantity = 95,
            minStockThreshold = 15,
            unitPriceKz = 600.0,
            category = "Reidratantes Orais",
            barcode = "560123490080"
        ),
        Medication(
            id = "med_009",
            tradeName = "Arteméter Injetável 80mg/mL",
            genericName = "Arteméter Solução Injetável",
            dosageForm = "Ampolas de 1mL (Cx com 6 ampolas)",
            batchNumber = "ART-5521",
            expirationDate = "10/01/2027",
            daysUntilExpiry = 98,
            stockQuantity = 18,
            minStockThreshold = 8,
            unitPriceKz = 5200.0,
            category = "Antimaláricos Hospitalares",
            barcode = "560123490097"
        ),
        Medication(
            id = "med_010",
            tradeName = "Salbutamol Spray 100mcg",
            genericName = "Sulfato de Salbutamol",
            dosageForm = "Inalador Pressurizado 200 doses",
            batchNumber = "SBT-9923",
            expirationDate = "01/11/2026",
            daysUntilExpiry = 28, // Dispara alerta de validade (< 30 dias)!
            stockQuantity = 12,
            minStockThreshold = 8,
            unitPriceKz = 4500.0,
            category = "Respiratórios",
            barcode = "560123490103"
        )
    )

    fun getInitialPrescriptions(): List<Prescription> = listOf(
        Prescription(
            code = "REC-2026-084",
            patientId = "pat_002",
            patientName = "Esperança Sebastião de Oliveira",
            patientBi = "007231456BE038",
            coverageType = "ENSA Seguros",
            doctorName = "Dra. Luísa Kiala",
            diagnosis = "Malária Não Complicada (P. falciparum) + Síndrome Febril",
            clinicalNotes = "Gota espessa positiva para Plasmodium falciparum (+). Iniciar Coartem com alimentação e manter hidratação abundante.",
            items = listOf(
                PrescriptionItem(
                    medicationId = "med_001",
                    tradeName = "Coartem 80/480mg",
                    genericName = "Arteméter + Lumefantrina",
                    dosage = "1 comprimido",
                    frequency = "De 12 em 12 horas",
                    duration = "Durante 3 dias (completar o esquema de 6 doses)",
                    instructions = "Tomar logo após refeição com teor lipídico ou leite para melhor absorção.",
                    quantity = 1,
                    unitPriceKz = 4800.0
                ),
                PrescriptionItem(
                    medicationId = "med_002",
                    tradeName = "Paracetamol 500mg",
                    genericName = "Paracetamol",
                    dosage = "1 comprimido (500mg)",
                    frequency = "De 8 em 8 horas se temperatura >= 37.8°C ou dor",
                    duration = "3 a 5 dias",
                    instructions = "Não ultrapassar 4 comprimidos por dia.",
                    quantity = 1,
                    unitPriceKz = 1200.0
                )
            ),
            status = PrescriptionStatus.PENDING_DISPENSE,
            createdAt = System.currentTimeMillis() - 20 * 60 * 1000
        )
    )

    fun getInitialSales(): List<SaleOrder> = listOf(
        SaleOrder(
            id = "sale_101",
            invoiceNumber = "FR 2026/0418",
            patientName = "João Baptista Afonso",
            patientBi = "003847291LN054",
            coverageType = "Nossa Seguros",
            items = listOf(
                CartItem(
                    medicationId = "med_004",
                    tradeName = "Omeprazol 20mg",
                    genericName = "Omeprazol",
                    batchNumber = "OMP-4412",
                    unitPriceKz = 2500.0,
                    quantity = 2
                )
            ),
            subtotalKz = 5000.0,
            discountKz = 0.0,
            totalKz = 5000.0,
            paymentMethod = PaymentMethod.TPA_MULTICAIXA,
            amountPaidKz = 5000.0,
            changeKz = 0.0,
            referenceOrPhone = "TPA-POS-78219",
            cashierName = "Teresa Gomes",
            timestamp = System.currentTimeMillis() - 95 * 60 * 1000
        )
    )
}
