import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class ElfAlignmentExtension @Inject constructor(objects: ObjectFactory) {

  /**
   * 最大对齐数
   */
  public val maxAlign: Property<Long> = objects.property(Long::class.java).convention(16384L)

  public val resolveOnBuild: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

  public val output: ElfAlignmentOutputExtension =
    objects.newInstance(ElfAlignmentOutputExtension::class.java)

  public fun output(action: Action<ElfAlignmentOutputExtension>) {
    action.execute(output)
  }

}

public abstract class ElfAlignmentOutputExtension @Inject constructor(objects: ObjectFactory) {
  public val csv: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
  public val html: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
  public val json: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}
