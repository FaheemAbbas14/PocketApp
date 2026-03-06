# Pocket App - Clean Architecture & SOLID Refactor

## Overview

Pocket App has been refactored to follow **clean architecture** and **SOLID principles** across both UI and data layers.

---

## Architecture Layers

### 1. **UI Layer** (`ui/`)
- **Thin Entry Point** (`MainActivity.kt`)
  - Only handles Android lifecycle + permissions
  - Delegates all UI composition to `PocketAppRoot`

- **App Shell** (`ui/app/PocketAppRoot.kt`)
  - Orchestrates authentication flow
  - Manages bottom navigation between 3 tabs
  - Routes dialogs and edit screens

- **Feature Modules**
  - `ui/auth/`: Authentication UI (login/register)
  - `ui/tasks/`: Tasks screens + dialogs
  - `ui/expenses/`: Expenses screens + dialogs
  - `ui/events/`: Events screens + dialogs
  - `ui/common/`: Shared utilities (date/time formatters)

- **ViewModel** (`MainViewModel.kt`)
  - Still acts as state holder
  - Coordinates repository calls
  - Manages UI state lifecycle

### 2. **Data Layer** (`data/repository/`)

#### AuthRepository
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    fun logout()
    fun observeCurrentUser(): Flow<String?>
}
```

#### TaskRepository
```kotlin
interface TaskRepository {
    suspend fun addTask(...): Result<Unit>
    suspend fun updateTask(item: TaskItem): Result<Unit>
    suspend fun deleteTask(item: TaskItem): Result<Unit>
    fun observeTasks(userId: String): Flow<List<TaskItem>>
}
```

#### ExpenseRepository
```kotlin
interface ExpenseRepository {
    suspend fun addExpense(...): Result<Unit>
    suspend fun updateExpense(item: ExpenseItem): Result<Unit>
    suspend fun deleteExpense(item: ExpenseItem): Result<Unit>
    fun observeExpenses(userId: String): Flow<List<ExpenseItem>>
}
```

#### EventRepository
```kotlin
interface EventRepository {
    suspend fun addEvent(...): Result<Unit>
    suspend fun updateEvent(item: EventItem): Result<Unit>
    suspend fun deleteEvent(item: EventItem): Result<Unit>
    fun observeEvents(userId: String): Flow<List<EventItem>>
}
```

---

## SOLID Principles Applied

### **S - Single Responsibility**
- Each feature module (tasks, expenses, events) is isolated
- Repositories handle only data access
- UI composables handle only presentation
- MainActivity handles only lifecycle

### **O - Open/Closed**
- New features can be added without modifying existing code
- New tabs/screens plugged into `PocketAppRoot` easily
- Repositories are open for extension via interfaces

### **L - Liskov Substitution**
- All repositories implement their interfaces consistently
- Can swap implementations (Firebase → local DB) without breaking UI

### **I - Interface Segregation**
- Focused repository interfaces (AuthRepository, TaskRepository, etc.)
- UI layer depends on contracts, not concrete implementations
- Each feature only imports what it needs

### **D - Dependency Inversion**
- UI depends on repository interfaces, not Firebase directly
- ViewModel coordinates repositories abstractly
- Easy to mock/test with dependency injection ready

---

## File Structure

```
app/src/main/java/com/faheem/pocketapp/
├── MainActivity.kt                          # Thin entry point
├── MainViewModel.kt                         # State holder (to be refactored)
├── AuthCache.kt                             # Local credential storage
├── AlarmScheduler.kt                        # Alarm/reminder logic
├── ReminderBroadcastReceiver.kt             # Broadcast handler
├── BootCompletedReceiver.kt                 # Boot receiver
│
├── data/
│   └── repository/
│       ├── AuthRepository.kt                # Auth interface + impl
│       ├── TaskRepository.kt                # Task interface + impl
│       ├── ExpenseRepository.kt             # Expense interface + impl
│       └── EventRepository.kt               # Event interface + impl
│
└── ui/
    ├── app/
    │   └── PocketAppRoot.kt                 # App shell & routing
    ├── auth/
    │   └── AuthScreen.kt                    # Auth UI
    ├── tasks/
    │   └── TasksUi.kt                       # Tasks screens + dialogs
    ├── expenses/
    │   └── ExpensesUi.kt                    # Expenses screens + dialogs
    ├── events/
    │   └── EventsUi.kt                      # Events screens + dialogs
    ├── common/
    │   └── DateTimeUtils.kt                 # Shared formatters
    └── theme/
        ├── Theme.kt
        ├── Color.kt
        └── Type.kt
```

---

## Next Steps (Phase 3)

1. **Inject Repositories into ViewModel**
   - Replace direct Firestore calls with repository methods
   - Use dependency injection (Hilt)

2. **Create Use-Cases Layer**
   - Encapsulate business logic per feature
   - E.g., `AddTaskUseCase`, `DeleteExpenseUseCase`

3. **Add Testing**
   - Mock repositories for UI tests
   - Unit tests for use-cases
   - Integration tests for repositories

4. **Optional: Gradle Module Split**
   - `:feature:auth`
   - `:feature:tasks`
   - `:feature:expenses`
   - `:feature:events`
   - `:data`
   - `:core`

---

## Compile Status

✅ **All layers compile without errors**

---

## Benefits

- **Testability**: Each layer can be tested independently
- **Reusability**: Repositories can serve multiple features
- **Maintainability**: Clear separation of concerns
- **Scalability**: Easy to add new features or swap implementations
- **Team Collaboration**: Different teams can work on different features without conflicts

---

**Version**: 2.0 (Clean Architecture + SOLID)  
**Date**: March 6, 2026

