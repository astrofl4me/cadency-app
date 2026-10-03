package br.edu.fsa.planner

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import br.edu.fsa.planner.data.repository.LocalAuthRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LocalAuthRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    // Android's FileStorage uses Unix rename semantics; use Okio's atomic move on Windows hosts.
    private fun createStore(scope: CoroutineScope, file: File) = PreferenceDataStoreFactory.create(
        storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { file.absolutePath.toPath() }, scope = scope)

    @Test fun sessionSurvivesReopeningStoreAndLogoutClearsIt() = runBlocking {
        val file = File(temporary.newFolder(), "session.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var store = createStore(scope, file)
        var repository = LocalAuthRepository(store)
        assertNull(repository.session.first())
        repository.signInDemo()
        val user = repository.session.first()!!
        assertEquals(1, store.data.first().asMap().size)
        assertEquals(user.email, store.data.first().asMap().values.single())
        scope.coroutineContext.job.cancelAndJoin()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        store = createStore(scope, file)
        repository = LocalAuthRepository(store)
        try {
            assertEquals(user, repository.session.first())
            repository.signOut()
            assertNull(repository.session.first())
            assertTrue(store.data.first().asMap().isEmpty())
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }

    @Test fun incorrectCredentialsDoNotCreateSession() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(temporary.newFolder(), "session.preferences_pb")
        val repository = LocalAuthRepository(createStore(scope, file))
        try {
            assertFalse(repository.signIn("invalid@example.com", "incorrect"))
            assertNull(repository.session.first())
        } finally { scope.coroutineContext.job.cancelAndJoin() }
    }
}
