package miroshka.allayideplugin.version

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class AllayVersionCheckStartupActivity : ProjectActivity {
    
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            checkAllayVersion(project)
        }
    }
    
    private fun checkAllayVersion(project: Project) {
        val buildFile = findBuildGradleKts(project) ?: return
        val currentVersion = extractAllayApiVersion(project, buildFile) ?: return
        
        val latestVersion = AllayVersionChecker.getLatestVersion() ?: return
        
        val comparison = AllayVersionChecker.compareVersions(currentVersion, latestVersion)
        
        if (comparison == AllayVersionChecker.VersionComparison.OUTDATED) {
            showUpdateNotification(project, currentVersion, latestVersion, buildFile)
        }
    }
    
    private fun findBuildGradleKts(project: Project): VirtualFile? {
        val basePath = project.basePath ?: return null
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath) ?: return null
        return baseDir.findChild("build.gradle.kts")
    }
    
    private fun extractAllayApiVersion(project: Project, buildFile: VirtualFile): String? {
        return try {
            com.intellij.openapi.application.ReadAction.compute<String?, Throwable> {
                val psiFile = PsiManager.getInstance(project).findFile(buildFile) as? KtFile
                    ?: return@compute null
            
                val allayBlocks = psiFile.collectDescendantsOfType<org.jetbrains.kotlin.psi.KtCallExpression> {
                    (it.calleeExpression as? org.jetbrains.kotlin.psi.KtNameReferenceExpression)?.getReferencedName() == "allay"
                }
                
                var result: String? = null
                for (block in allayBlocks) {
                    val lambda = block.lambdaArguments.firstOrNull()?.getLambdaExpression()
                    val bodyExpression = lambda?.bodyExpression ?: continue
                    
                    bodyExpression.statements.forEach { statement ->
                        if (statement is org.jetbrains.kotlin.psi.KtBinaryExpression) {
                            val left = statement.left as? org.jetbrains.kotlin.psi.KtNameReferenceExpression
                            if (left?.getReferencedName() == "api") {
                                val right = statement.right as? KtStringTemplateExpression
                                val version = right?.entries?.firstOrNull()?.text
                                if (version != null) {
                                    result = version
                                }
                            }
                        }
                    }
                    if (result != null) break
                }
                
                result
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showUpdateNotification(
        project: Project,
        currentVersion: String,
        latestVersion: String,
        buildFile: VirtualFile
    ) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Allay Version Checker")
            .createNotification(
                "Allay API Update Available",
                "Current version: $currentVersion\nLatest version: $latestVersion",
                NotificationType.INFORMATION
            )
        
        notification.addAction(object : AnAction("Update to $latestVersion") {
            override fun actionPerformed(e: AnActionEvent) {
                updateAllayVersion(project, buildFile, latestVersion)
                notification.expire()
            }
        })
        
        notification.addAction(object : AnAction("Ignore") {
            override fun actionPerformed(e: AnActionEvent) {
                notification.expire()
            }
        })
        
        notification.notify(project)
    }
    
    private fun updateAllayVersion(project: Project, buildFile: VirtualFile, newVersion: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val psiFile = PsiManager.getInstance(project).findFile(buildFile) as? KtFile ?: return@runWriteCommandAction
            
            val allayBlocks = psiFile.collectDescendantsOfType<org.jetbrains.kotlin.psi.KtCallExpression> {
                (it.calleeExpression as? org.jetbrains.kotlin.psi.KtNameReferenceExpression)?.getReferencedName() == "allay"
            }
            
            for (block in allayBlocks) {
                val lambda = block.lambdaArguments.firstOrNull()?.getLambdaExpression()
                val bodyExpression = lambda?.bodyExpression ?: continue
                
                bodyExpression.statements.forEach { statement ->
                    if (statement is org.jetbrains.kotlin.psi.KtBinaryExpression) {
                        val left = statement.left as? org.jetbrains.kotlin.psi.KtNameReferenceExpression
                        if (left?.getReferencedName() == "api") {
                            val right = statement.right as? KtStringTemplateExpression
                            if (right != null) {
                                val factory = KtPsiFactory(project)
                                val newExpression = factory.createExpression("\"$newVersion\"")
                                right.replace(newExpression)
                                
                                LocalFileSystem.getInstance().refresh(false)
                                
                                NotificationGroupManager.getInstance()
                                    .getNotificationGroup("Allay Version Checker")
                                    .createNotification(
                                        "Allay API Updated",
                                        "Successfully updated to version $newVersion",
                                        NotificationType.INFORMATION
                                    )
                                    .notify(project)
                                
                                return@runWriteCommandAction
                            }
                        }
                    }
                }
            }
        }
    }
}

