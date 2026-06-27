package com.example.overloadtracker.ui

enum class MuscleRegion {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, QUADS, HAMSTRINGS, CALVES, ABS, GLUTES, FOREARMS, TRAPS
}

data class ExerciseInfo(
    val name: String,
    val description: String,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val difficulty: String,
    val targetRegions: List<MuscleRegion>
)

object ExerciseInfoGenerator {

    fun generate(exerciseName: String): ExerciseInfo {
        val name = exerciseName.lowercase()
        
        val primaryMusclesNames = mutableListOf<String>()
        val secondaryMusclesNames = mutableListOf<String>()
        val instructions = mutableListOf<String>()
        val targetRegions = mutableListOf<MuscleRegion>()
        var difficulty = "Orta"
        var targetPart = "vücut"

        // --- ENHANCED IDENTIFICATION LOGIC ---
        
        // 1. BACK / SIRT (Check first for 'reverse' moves that are often misidentified)
        if (name.contains("back") || name.contains("row") || name.contains("lat") || 
            name.contains("pull") || name.contains("chin up") || name.contains("reverse fly") || 
            name.contains("face pull") || name.contains("shrug")) {
            
            primaryMusclesNames.add("Sırt (Latissimus Dorsi, Rhomboids)")
            targetRegions.add(MuscleRegion.BACK)
            targetPart = "sırt"
            
            if (name.contains("reverse fly") || name.contains("face pull")) {
                primaryMusclesNames.clear()
                primaryMusclesNames.add("Arka Omuz (Rear Deltoid)")
                secondaryMusclesNames.add("Sırt")
                targetRegions.clear()
                targetRegions.add(MuscleRegion.SHOULDERS)
                targetRegions.add(MuscleRegion.BACK)
                instructions.addAll(listOf(
                    "Dirseklerinizi hafif bükülü tutarak ağırlığı yanlara doğru açın.",
                    "Kürek kemiklerinizi birbirine yaklaştırarak arka omuzlarınızı sıkıştırın.",
                    "Kontrollü bir şekilde başlangıç pozisyonuna dönün."
                ))
            } else if (name.contains("shrug")) {
                primaryMusclesNames.clear()
                primaryMusclesNames.add("Trapez (Trapezius)")
                targetRegions.add(MuscleRegion.TRAPS)
                instructions.addAll(listOf("Omuzlarınızı kulaklarınıza doğru çekin.", "En üst noktada bekleyip yavaşça bırakın."))
            } else {
                secondaryMusclesNames.addAll(listOf("Biceps", "Arka Omuz"))
                instructions.addAll(listOf(
                    "Ağırlığı çekerken dirseklerinizi gövdenize yakın tutun.",
                    "Göğsünüzü öne çıkarın ve sırtınızdaki kasılmayı hissedin.",
                    "Omuzlarınızı aşağıda tutmaya özen gösterin."
                ))
            }
        }
        
        // 2. CHEST / GÖĞÜS (Excluding reverse moves)
        else if (name.contains("bench") || name.contains("chest") || (name.contains("press") && (name.contains("chest") || name.contains("incline") || name.contains("decline"))) || 
            name.contains("fly") || name.contains("crossover") || name.contains("pec deck") || name.contains("push up")) {
            
            primaryMusclesNames.add("Göğüs (Pectoralis Major)")
            targetRegions.add(MuscleRegion.CHEST)
            secondaryMusclesNames.addAll(listOf("Triceps", "Ön Omuz"))
            targetPart = "göğüs"
            
            if (name.contains("fly") || name.contains("crossover")) {
                instructions.addAll(listOf(
                    "Kabloları veya dambılları geniş bir yay çizerek göğüs hizasında birleştirin.",
                    "Dirseklerinizi hafif bükülü tutarak göğüs kaslarınızı gerin.",
                    "Merkezde kasları 1 saniye boyunca sıkıştırın."
                ))
                secondaryMusclesNames.remove("Triceps")
            } else {
                instructions.addAll(listOf(
                    "Ağırlığı göğüs hizanıza kadar kontrollü bir şekilde indirin.",
                    "Patlayıcı bir kuvvetle yukarı itin.",
                    "Kürek kemiklerinizi sehpaya sabitleyerek göğsünüzü öne çıkarın."
                ))
            }
        }
        
        // 3. LEGS / BACAK
        else if (name.contains("leg") || name.contains("squat") || name.contains("lunge") || name.contains("calf") || name.contains("quad") || name.contains("hamstring") || name.contains("deadlift")) {
            targetPart = "alt vücut"
            difficulty = "Zor"
            
            if (name.contains("calf")) {
                primaryMusclesNames.add("Kalf (Gastrocnemius)")
                targetRegions.add(MuscleRegion.CALVES)
                instructions.addAll(listOf("Topuklarınızı yukarı kaldırabildiğiniz kadar kaldırın.", "Balet duruşu yapıyormuş gibi en üstte bekleyin."))
            } else if (name.contains("deadlift")) {
                primaryMusclesNames.addAll(listOf("Alt Sırt", "Hamstrings", "Kalça"))
                targetRegions.addAll(listOf(MuscleRegion.BACK, MuscleRegion.HAMSTRINGS, MuscleRegion.GLUTES))
                instructions.addAll(listOf("Barı kaval kemiğinize yakın tutun.", "Sırtınızı düz tutarak kalçanızdan güç alıp kalkın."))
            } else {
                primaryMusclesNames.add("Bacak (Quadriceps, Hamstrings)")
                targetRegions.addAll(listOf(MuscleRegion.QUADS, MuscleRegion.HAMSTRINGS, MuscleRegion.GLUTES))
                instructions.addAll(listOf(
                    "Kalçanızı geriye vererek alçalın.",
                    "Dizlerinizin ayak uçlarıyla hizalı kalmasına dikkat edin.",
                    "Karın kaslarınızı hareket boyunca sıkı tutun."
                ))
            }
        }
        
        // 4. SHOULDERS / OMUZ
        else if (name.contains("shoulder") || name.contains("press") || name.contains("raise") || name.contains("deltoid")) {
            primaryMusclesNames.add("Omuz (Deltoids)")
            targetRegions.add(MuscleRegion.SHOULDERS)
            secondaryMusclesNames.add("Üst Trapez")
            targetPart = "omuz"
            
            if (name.contains("lateral") || name.contains("raise")) {
                instructions.addAll(listOf("Kollarınızı yanlara omuz hizasına kadar açın.", "Dirseklerinizi hafif bükün."))
            } else {
                instructions.addAll(listOf("Ağırlıkları başınızın üzerine doğru kontrollü itin.", "Boynunuzu kasmayın."))
            }
        }
        
        // 5. ARMS / KOLLAR
        else if (name.contains("bicep") || name.contains("curl") || name.contains("hammer")) {
            primaryMusclesNames.add("Biceps (Biceps Brachii)")
            targetRegions.add(MuscleRegion.BICEPS)
            targetPart = "pazı"
            instructions.addAll(listOf("Dirseklerinizi sabitleyin.", "Sadece ön kolunuzu hareket ettirerek bükün."))
        } else if (name.contains("tricep") || name.contains("pushdown") || name.contains("extension") || name.contains("dips")) {
            primaryMusclesNames.add("Triceps (Triceps Brachii)")
            targetRegions.add(MuscleRegion.TRICEPS)
            targetPart = "arka kol"
            instructions.addAll(listOf("Kolunuzu tam gergin konuma getirin.", "Triceps kasınızdaki gerilimi hissedin."))
        }

        // --- DYNAMIC DESCRIPTION ---
        if (primaryMusclesNames.isEmpty()) {
            primaryMusclesNames.add("Genel Vücut")
            targetRegions.add(MuscleRegion.ABS)
            instructions.add("Hareketi nizami bir formda gerçekleştirin.")
        }

        val dynamicDescription = when {
            name.contains("cable") -> "$exerciseName, kablo sistemi ile kas üzerinde sürekli gerilim sağlayan teknik bir $targetPart hareketidir."
            name.contains("dumbbell") -> "$exerciseName, dambıllar ile yapılan ve her iki kolun bağımsız çalışmasını sağlayan bir $targetPart hareketidir."
            name.contains("barbell") -> "$exerciseName, halter kullanılarak yapılan ve yüksek ağırlık kapasitesi sunan temel bir $targetPart hareketidir."
            else -> "$exerciseName, $targetPart bölgesini hedefleyen ve antrenman verimliliğini artıran etkili bir harekettir."
        }

        return ExerciseInfo(
            name = exerciseName,
            description = dynamicDescription,
            primaryMuscles = primaryMusclesNames,
            secondaryMuscles = secondaryMusclesNames,
            instructions = instructions,
            difficulty = difficulty,
            targetRegions = targetRegions
        )
    }
}