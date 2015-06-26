package io.protostuff.compiler.parser;

import io.protostuff.compiler.model.Extension;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Import;
import io.protostuff.compiler.model.Proto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public final class ProtoExtensionRegistry extends AbstractExtensionRegistry {

    private final ExtensionRegistry localExtensionRegistry;
    private final ProtoContext context;
    private Proto proto;
    private final ConcurrentMap<String, Collection<Extension>> extensionCache = new ConcurrentHashMap<>();

    public ProtoExtensionRegistry(ProtoContext context) {
        this.context = context;
        this.proto = context.getProto();
        this.localExtensionRegistry = new LocalExtensionRegistry();
    }

    @Override
    public void registerExtension(Extension extension) {
        super.registerExtension(extension);
        localExtensionRegistry.registerExtension(extension);
        String fullName = extension.getExtendee().getFullName();
        extensionCache.remove(fullName);
    }

    @Override
    public Collection<Extension> getExtensions(String fullMessageName) {
        return extensionCache.computeIfAbsent(fullMessageName, name -> {
            Collection<Extension> result = new ArrayList<>();
            result.addAll(localExtensionRegistry.getExtensions(name));
            Deque<Import> queue = new ArrayDeque<>();
            queue.addAll(proto.getImports());
            while (!queue.isEmpty()) {
                Import anImport = queue.poll();
                Proto proto = anImport.getProto();
                Collection<Extension> extensions = getExtensions(proto, name);
                result.addAll(extensions);
                queue.addAll(proto.getPublicImports());
            }
            return result;
        });
    }

    private Collection<Extension> getExtensions(Proto proto, String name) {
        ProtoContext context = proto.getContext();
        ExtensionRegistry extensionRegistry = context.getExtensionRegistry();
        return extensionRegistry.getExtensions(name);
    }

}
