package miroshka.allayideplugin

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil
import javax.swing.Icon

object AllayIcons {
    private val ORIGINAL_ICON: Icon = IconLoader.getIcon("/icons/allay-chan-40x.png", AllayIcons::class.java)
    
    @JvmField
    val MODULE_ICON: Icon = IconUtil.scale(ORIGINAL_ICON, null, 0.4f)
    
    @JvmField
    val FILE_ICON: Icon = IconUtil.scale(ORIGINAL_ICON, null, 0.4f)
}

