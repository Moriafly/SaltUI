/* SPDX-License-Identifier: Apache-2.0 */

package saltui.ktlint;

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3;
import com.pinterest.ktlint.rule.engine.core.api.Rule;
import com.pinterest.ktlint.rule.engine.core.api.RuleId;
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider;
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import org.jetbrains.kotlin.com.intellij.lang.ASTNode;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtNullableType;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.jetbrains.kotlin.psi.KtUserType;

public final class ComposableModifierRuleSet extends RuleSetProviderV3 {
    public ComposableModifierRuleSet() {
        super(new RuleSetId("saltui"));
    }

    @Override
    public Set<RuleProvider> getRuleProviders() {
        return Set.of(RuleProvider.Companion.invoke(ComposableModifierRule::new));
    }

    private static final class ComposableModifierRule extends Rule {
        ComposableModifierRule() {
            super(new RuleId("saltui:composable-modifier-parameter"), new About(), Set.of(), Set.of());
        }

        @Override
        public void beforeVisitChildNodes(
                ASTNode node,
                boolean autoCorrect,
                Function3<? super Integer, ? super String, ? super Boolean, Unit> emit) {
            if (!(node.getPsi() instanceof KtNamedFunction function)) return;
            var file = function.getContainingKtFile();
            var composable = function.getAnnotationEntries().stream().anyMatch(annotation ->
                    hasType(annotation.getTypeReference(), file, "androidx.compose.runtime.Composable"));
            if (!composable) return;

            var modifiers = function.getValueParameters().stream().filter(parameter ->
                    hasType(parameter.getTypeReference(), file, "androidx.compose.ui.Modifier")).toList();
            if (modifiers.size() > 1) {
                emit.invoke(modifiers.get(1).getTextOffset(),
                        "Composable functions may have at most one Modifier parameter.", false);
            } else if (modifiers.size() == 1 && !"modifier".equals(modifiers.get(0).getName())) {
                emit.invoke(modifiers.get(0).getTextOffset(),
                        "A Composable's Modifier parameter must be named `modifier`.", false);
            }
        }
    }

    // Match Kotlin syntax without treating slots, functions or collections as Modifier parameters.
    private static boolean hasType(KtTypeReference reference, KtFile file, String qualifiedName) {
        if (reference == null) return false;
        var type = reference.getTypeElement();
        if (type instanceof KtNullableType nullable) type = nullable.getInnerType();
        if (!(type instanceof KtUserType userType)) return false;
        if (userType.getQualifier() != null) return qualifiedName.equals(userType.getText());

        var name = userType.getReferencedName();
        for (var directive : file.getImportDirectives()) {
            var imported = directive.getImportedFqName();
            if (imported == null || directive.isAllUnder()) continue;
            var alias = directive.getAliasName();
            var localName = alias != null ? alias : imported.shortName().asString();
            if (localName.equals(name)) return qualifiedName.equals(imported.asString());
        }
        var simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
        return simpleName.equals(name);
    }
}
