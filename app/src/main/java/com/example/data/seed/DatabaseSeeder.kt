package com.example.data.seed

import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.PaperDao
import com.example.data.local.QuestionPaperEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.local.encodeNoteFiles
import com.example.data.model.ExamType
import com.example.data.model.Role
import com.example.data.storage.LocalStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class DatabaseSeeder(
    private val userDao: UserDao,
    private val noteDao: NoteDao,
    private val paperDao: PaperDao,
    private val storageService: LocalStorageService
) {

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        val admin = ensureAdminUser()
        val student = ensureDemoStudent()

        if (noteDao.countNotes() == 0) {
            seedNotes(admin, student)
        }
        if (paperDao.countPapers() == 0) {
            seedPapers(admin, student)
        }
    }

    private suspend fun ensureAdminUser(): UserEntity {
        val existing = userDao.getByEmail("admin@cloudnotes.edu")
        if (existing != null) return existing
        val admin = UserEntity(
            id = "user-admin-001",
            email = "admin@cloudnotes.edu",
            passwordHash = hashPassword("Admin@CloudNotes2026"),
            name = "System Administrator",
            branch = "Computer Science",
            semester = 4,
            rolesCsv = "${Role.ROLE_ADMIN.name},${Role.ROLE_STUDENT.name}",
            enabled = true,
            createdAt = System.currentTimeMillis() - 86_400_000L * 14
        )
        userDao.insert(admin)
        return admin
    }

    private suspend fun ensureDemoStudent(): UserEntity {
        val existing = userDao.getByEmail("student@cloudnotes.edu")
        if (existing != null) return existing
        val student = UserEntity(
            id = "user-student-001",
            email = "student@cloudnotes.edu",
            passwordHash = hashPassword("Student@2026"),
            name = "Aarav Sharma",
            branch = "Computer Science",
            semester = 4,
            rolesCsv = Role.ROLE_STUDENT.name,
            enabled = true,
            createdAt = System.currentTimeMillis() - 86_400_000L * 10
        )
        userDao.insert(student)
        return student
    }

    private suspend fun seedNotes(admin: UserEntity, student: UserEntity) {
        val now = System.currentTimeMillis()
        val osPdf = storageService.createSamplePdf(
            originalFileName = "OS_Memory_Management_Unit3.pdf",
            headerTitle = "Operating Systems: Memory Management",
            subtitle = "Computer Science • Semester 4 • Lecture Notes",
            sections = listOf(
                "1. Paging and Virtual Memory" to listOf(
                    "Paging divides physical memory into fixed-size frames and logical memory into blocks of the same size called pages.",
                    "The Translation Lookaside Buffer (TLB) is a hardware cache that accelerates virtual-to-physical address translation.",
                    "Page fault handling traps to the OS kernel when a referenced page is marked invalid in the page table."
                ),
                "2. Page Replacement Algorithms" to listOf(
                    "FIFO replaces the oldest loaded page, though it can suffer from Belady's Anomaly.",
                    "LRU (Least Recently Used) approximates optimal replacement by tracking recent memory references.",
                    "Working set models help prevent thrashing by ensuring each active process retains its active page frames."
                ),
                "3. Segmentation & Protection" to listOf(
                    "Segmentation supports a user view of memory with variable-length logical segments (code, stack, heap).",
                    "Each segment table entry stores a base address and limit register for bounds checking."
                )
            )
        )

        val dsaPdf = storageService.createSamplePdf(
            originalFileName = "DSA_Graphs_Dynamic_Programming.pdf",
            headerTitle = "Data Structures & Algorithms: Graphs & DP",
            subtitle = "Computer Science • Semester 3 • Complete Study Guide",
            sections = listOf(
                "1. Graph Traversals & Shortest Paths" to listOf(
                    "Breadth-First Search (BFS) explores level by level in O(V + E) time and finds shortest paths in unweighted graphs.",
                    "Dijkstra's Algorithm computes single-source shortest paths for non-negative edge weights in O((V + E) log V).",
                    "Topological Sort orders vertices in a Directed Acyclic Graph (DAG) for dependency scheduling."
                ),
                "2. Dynamic Programming Patterns" to listOf(
                    "Optimal substructure and overlapping subproblems characterize DP problems like 0/1 Knapsack and LCS.",
                    "Memoization uses top-down recursion with caching, whereas tabulation builds solutions bottom-up."
                )
            )
        )

        val dbmsPdf = storageService.createSamplePdf(
            originalFileName = "DBMS_Normalization_Transactions.pdf",
            headerTitle = "Database Management Systems: Normalization & ACID",
            subtitle = "Information Technology • Semester 5 • Module Notes",
            sections = listOf(
                "1. Normal Forms (1NF to BCNF)" to listOf(
                    "First Normal Form (1NF) eliminates repeating groups and enforces atomic column values.",
                    "Third Normal Form (3NF) removes transitive dependencies of non-prime attributes on candidate keys.",
                    "Boyce-Codd Normal Form (BCNF) requires every non-trivial functional dependency X -> Y to have X as a superkey."
                ),
                "2. ACID Properties & Concurrency Control" to listOf(
                    "Atomicity, Consistency, Isolation, and Durability guarantee reliable transaction execution.",
                    "Strict Two-Phase Locking (2PL) prevents cascading aborts and guarantees conflict serializability."
                )
            )
        )

        val cnPdf = storageService.createSamplePdf(
            originalFileName = "Computer_Networks_TCP_IP_Layering.pdf",
            headerTitle = "Computer Networks: Transport & Network Layers",
            subtitle = "Computer Science • Semester 5 • Exam Prep Notes",
            sections = listOf(
                "1. TCP Congestion & Flow Control" to listOf(
                    "Three-way handshake (SYN, SYN-ACK, ACK) establishes reliable full-duplex connections.",
                    "Sliding window flow control prevents a fast sender from overwhelming a slow receiver buffer.",
                    "AIMD congestion control combines Slow Start, Congestion Avoidance, and Fast Retransmit."
                ),
                "2. IP Routing Protocols" to listOf(
                    "OSPF uses link-state routing with Dijkstra's shortest path first algorithm inside an Autonomous System.",
                    "BGP acts as the path-vector inter-domain routing protocol connecting autonomous systems across the Internet."
                )
            )
        )

        val ecePdf = storageService.createSamplePdf(
            originalFileName = "Digital_Signal_Processing_FFT.pdf",
            headerTitle = "Digital Signal Processing: DFT & FFT",
            subtitle = "Electronics • Semester 6 • Lecture Summary",
            sections = listOf(
                "1. Discrete Fourier Transform (DFT)" to listOf(
                    "Converts N-point finite-duration discrete signals from time domain into equally spaced frequency samples.",
                    "Circular convolution in the time domain corresponds to pointwise multiplication in the DFT domain."
                ),
                "2. Fast Fourier Transform (Cooley-Tukey)" to listOf(
                    "Radix-2 Decimation-in-Time FFT reduces computational complexity from O(N^2) to O(N log N).",
                    "Butterfly operations exploit symmetry and periodicity of twiddle factors."
                )
            )
        )

        val mechPdf = storageService.createSamplePdf(
            originalFileName = "Thermodynamics_Laws_Entropy.pdf",
            headerTitle = "Engineering Thermodynamics: Laws & Cycles",
            subtitle = "Mechanical • Semester 4 • Reference Notes",
            sections = listOf(
                "1. First and Second Laws of Thermodynamics" to listOf(
                    "Conservation of energy for closed systems: delta Q = dU + delta W.",
                    "Clausius inequality defines entropy generation for irreversible cycles and Carnot efficiency bounds."
                ),
                "2. Rankine & Brayton Power Cycles" to listOf(
                    "Ideal Rankine cycle models steam turbine power plants with isentropic pumping and expansion.",
                    "Regeneration and reheat stages raise mean temperature of heat addition to improve thermal efficiency."
                )
            )
        )

        val seededNotes = listOf(
            NoteEntity(
                id = "note-os-001",
                title = "Operating Systems Unit 3: Memory Management & Virtual Memory",
                subject = "Operating Systems",
                branch = "Computer Science",
                semester = 4,
                description = "Detailed lecture notes covering paging, TLB caching, page replacement algorithms (FIFO, LRU, Optimal), and segmentation.",
                uploaderId = student.id,
                uploaderName = student.name,
                filesJson = encodeNoteFiles(listOf(osPdf)),
                totalSize = osPdf.size,
                downloadCount = 42,
                bookmarkCount = 15,
                createdAt = now - 86_400_000L * 5,
                updatedAt = now - 86_400_000L * 5
            ),
            NoteEntity(
                id = "note-dsa-002",
                title = "Data Structures & Algorithms: Graph Algorithms and DP",
                subject = "Data Structures & Algorithms",
                branch = "Computer Science",
                semester = 3,
                description = "High-yield notes on BFS, DFS, Dijkstra, Topological Sort, 0/1 Knapsack, and memoization vs tabulation.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(dsaPdf)),
                totalSize = dsaPdf.size,
                downloadCount = 67,
                bookmarkCount = 24,
                createdAt = now - 86_400_000L * 4,
                updatedAt = now - 86_400_000L * 4
            ),
            NoteEntity(
                id = "note-dbms-003",
                title = "DBMS Normalization, Functional Dependencies & ACID Transactions",
                subject = "Database Management Systems",
                branch = "Information Technology",
                semester = 5,
                description = "Complete walkthrough of 1NF, 2NF, 3NF, BCNF decomposition and strict 2PL concurrency control.",
                uploaderId = student.id,
                uploaderName = student.name,
                filesJson = encodeNoteFiles(listOf(dbmsPdf)),
                totalSize = dbmsPdf.size,
                downloadCount = 31,
                bookmarkCount = 11,
                createdAt = now - 86_400_000L * 3,
                updatedAt = now - 86_400_000L * 3
            ),
            NoteEntity(
                id = "note-cn-004",
                title = "Computer Networks: TCP Congestion Control & OSPF/BGP Routing",
                subject = "Computer Networks",
                branch = "Computer Science",
                semester = 5,
                description = "Concise exam revision sheets for TCP sliding window, AIMD congestion control, and IP routing protocols.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(cnPdf)),
                totalSize = cnPdf.size,
                downloadCount = 28,
                bookmarkCount = 9,
                createdAt = now - 86_400_000L * 2,
                updatedAt = now - 86_400_000L * 2
            ),
            NoteEntity(
                id = "note-dsp-005",
                title = "Digital Signal Processing: DFT Properties & Radix-2 FFT",
                subject = "Digital Signal Processing",
                branch = "Electronics",
                semester = 6,
                description = "Step-by-step derivation of butterfly diagrams, twiddle factor symmetries, and circular convolution.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(ecePdf)),
                totalSize = ecePdf.size,
                downloadCount = 19,
                bookmarkCount = 7,
                createdAt = now - 86_400_000L,
                updatedAt = now - 86_400_000L
            ),
            NoteEntity(
                id = "note-thermo-006",
                title = "Engineering Thermodynamics: Entropy, Rankine & Brayton Cycles",
                subject = "Thermodynamics",
                branch = "Mechanical",
                semester = 4,
                description = "Formulas, P-v and T-s diagrams, and solved cycle efficiency analysis for mid-semester revision.",
                uploaderId = student.id,
                uploaderName = student.name,
                filesJson = encodeNoteFiles(listOf(mechPdf)),
                totalSize = mechPdf.size,
                downloadCount = 14,
                bookmarkCount = 5,
                createdAt = now - 36_000_000L,
                updatedAt = now - 36_000_000L
            )
        )

        noteDao.insertAll(seededNotes)
    }

    private suspend fun seedPapers(admin: UserEntity, student: UserEntity) {
        val now = System.currentTimeMillis()
        val osPaper1 = storageService.createSamplePdf(
            originalFileName = "OS_EndSem_Dec2024_QP.pdf",
            headerTitle = "Operating Systems - End Semester Examination (Dec 2024)",
            subtitle = "Max Marks: 100 • Time: 3 Hours • Computer Science Sem 4",
            sections = listOf(
                "Part A: Short Answer Questions (5 x 4 = 20 Marks)" to listOf(
                    "Q1. Differentiate between user-level threads and kernel-level threads.",
                    "Q2. Define Belady's Anomaly and identify which page replacement policy exhibits it.",
                    "Q3. Explain the four necessary conditions for a deadlock to occur."
                ),
                "Part B: Analytical & Design Questions (4 x 20 = 80 Marks)" to listOf(
                    "Q4. Given processes P1..P4 with arrival and burst times, compute average waiting time under SJF and Round Robin (q=3ms).",
                    "Q5. Solve the Banker's Algorithm safety sequence for 5 processes and 3 resource types."
                )
            )
        )
        val osPaperScheme = storageService.createSamplePdf(
            originalFileName = "OS_EndSem_Dec2024_MarkingScheme.pdf",
            headerTitle = "Operating Systems - Official Evaluation Scheme",
            subtitle = "Solution Key & Marks Distribution • Dec 2024",
            sections = listOf(
                "Key Solutions & Rubric" to listOf(
                    "Q1: Award 2 marks for scheduling overhead comparison, 2 marks for blocking behavior.",
                    "Q4: Gantt chart carries 8 marks; waiting/turnaround calculations carry 12 marks.",
                    "Q5: Need matrix (5m), Safe sequence verification (10m), Request grant check (5m)."
                )
            )
        )

        val dsaPaper = storageService.createSamplePdf(
            originalFileName = "DSA_MidSem_2025_QP.pdf",
            headerTitle = "Data Structures & Algorithms - Mid Semester Test (2025)",
            subtitle = "Max Marks: 50 • Time: 90 Minutes • Computer Science Sem 3",
            sections = listOf(
                "Section I (25 Marks)" to listOf(
                    "Q1. Derive the worst-case and amortized time complexity of dynamic array resizing.",
                    "Q2. Construct an AVL tree by inserting keys: 30, 20, 40, 10, 25, 35, 50, 5."
                ),
                "Section II (25 Marks)" to listOf(
                    "Q3. Write an algorithm to detect a cycle in a directed graph using DFS colors.",
                    "Q4. Trace Dijkstra's shortest path algorithm from source vertex S on the given weighted graph."
                )
            )
        )

        val dbmsPaper = storageService.createSamplePdf(
            originalFileName = "DBMS_EndSem_2024_QP.pdf",
            headerTitle = "Database Management Systems - End Semester Exam (2024)",
            subtitle = "Max Marks: 100 • Time: 3 Hours • Information Technology Sem 5",
            sections = listOf(
                "Questions" to listOf(
                    "Q1. Design an ER diagram for a University Course Registration System and convert to relational schema.",
                    "Q2. Normalize relation R(A, B, C, D, E) with FDs {AB->C, C->D, D->E} up to BCNF.",
                    "Q3. Test conflict serializability using precedence graphs for schedules S1 and S2."
                )
            )
        )

        val ecePaper = storageService.createSamplePdf(
            originalFileName = "Signals_Systems_Supplementary_2024.pdf",
            headerTitle = "Signals & Systems - Supplementary Examination (2024)",
            subtitle = "Max Marks: 100 • Electronics Sem 4",
            sections = listOf(
                "Core Questions" to listOf(
                    "Q1. State and prove Parseval's energy theorem for Fourier Transforms.",
                    "Q2. Determine the Z-transform and Region of Convergence (ROC) for x[n] = (0.5)^n u[n] - 2^n u[-n-1]."
                )
            )
        )

        val seededPapers = listOf(
            QuestionPaperEntity(
                id = "paper-os-2024",
                title = "Operating Systems End-Semester Question Paper & Marking Scheme",
                subject = "Operating Systems",
                branch = "Computer Science",
                semester = 4,
                year = 2024,
                examType = ExamType.END_SEMESTER.name,
                description = "Official university end-semester question paper paired with the faculty marking rubric.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(osPaper1, osPaperScheme)),
                totalSize = osPaper1.size + osPaperScheme.size,
                createdAt = now - 86_400_000L * 6,
                updatedAt = now - 86_400_000L * 6
            ),
            QuestionPaperEntity(
                id = "paper-dsa-2025",
                title = "Data Structures & Algorithms Mid-Semester Test 2025",
                subject = "Data Structures & Algorithms",
                branch = "Computer Science",
                semester = 3,
                year = 2025,
                examType = ExamType.MID_SEMESTER.name,
                description = "Covers AVL trees, amortized analysis, graph cycle detection, and shortest path problems.",
                uploaderId = student.id,
                uploaderName = student.name,
                filesJson = encodeNoteFiles(listOf(dsaPaper)),
                totalSize = dsaPaper.size,
                createdAt = now - 86_400_000L * 3,
                updatedAt = now - 86_400_000L * 3
            ),
            QuestionPaperEntity(
                id = "paper-dbms-2024",
                title = "DBMS End-Semester Examination Paper (2024)",
                subject = "Database Management Systems",
                branch = "Information Technology",
                semester = 5,
                year = 2024,
                examType = ExamType.END_SEMESTER.name,
                description = "Includes ER modeling, BCNF decomposition, SQL queries, and transaction serializability.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(dbmsPaper)),
                totalSize = dbmsPaper.size,
                createdAt = now - 86_400_000L * 2,
                updatedAt = now - 86_400_000L * 2
            ),
            QuestionPaperEntity(
                id = "paper-ece-2024",
                title = "Signals & Systems Supplementary Exam Paper",
                subject = "Signals & Systems",
                branch = "Electronics",
                semester = 4,
                year = 2024,
                examType = ExamType.SUPPLEMENTARY.name,
                description = "Fourier series, Laplace & Z-transform ROC problems from the supplementary session.",
                uploaderId = admin.id,
                uploaderName = admin.name,
                filesJson = encodeNoteFiles(listOf(ecePaper)),
                totalSize = ecePaper.size,
                createdAt = now - 86_400_000L,
                updatedAt = now - 86_400_000L
            )
        )

        paperDao.insertAll(seededPapers)
    }

    companion object {
        fun hashPassword(raw: String): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
