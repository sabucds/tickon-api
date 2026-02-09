# 📝 Pull Request Templates

Choose the template that best fits your PR:

## 🎯 Available Templates

### 🔹 [Default Template](../pull_request_template.md)
Use for general PRs or when other templates don't fit.

### ✨ [Feature Template](feature.md)
Use when:
- Adding new functionality
- Implementing user stories
- Creating new bounded contexts or modules

**Best for:** New features, enhancements, capabilities

---

### 🐛 [Bug Fix Template](bugfix.md)
Use when:
- Fixing defects or issues
- Addressing production incidents
- Resolving reported bugs

**Best for:** Bug fixes, hot fixes, patches

---

### ♻️ [Refactoring Template](refactoring.md)
Use when:
- Improving code structure
- Reducing technical debt
- Enhancing maintainability
- No behavior changes

**Best for:** Code quality improvements, architectural changes, refactoring

---

## 📖 How to Use

### On GitHub
When creating a PR, add `?template=template-name.md` to the URL:
```
https://github.com/user/repo/compare/main...feature-branch?template=feature.md
```

Or use the template dropdown in the GitHub UI.

### Locally
Copy the relevant template content when creating your PR description.

---

## 🎨 Template Guidelines

### All PRs Should Include:
- ✅ Clear, descriptive title
- 📋 Summary of changes
- 🧪 Testing strategy
- 📚 Updated documentation
- ✅ Completed checklist

### Architecture Compliance:
- 🏗️ Follow Hexagonal Architecture
- 🔷 Respect bounded contexts
- 📦 Use common module for shared abstractions
- 🔗 Cross-module communication via QueryBus/Events

### Code Quality:
- ✨ Formatted with `mvn spotless:apply`
- ✅ All tests passing
- 📏 Follows SOLID principles
- 🎯 Conventional commits

---

## 💡 Tips for Great PRs

1. **Keep it focused** - One concern per PR
2. **Make it reviewable** - Under 400 lines of changes
3. **Test thoroughly** - Include unit, integration tests
4. **Document well** - Clear descriptions and comments
5. **Self-review first** - Review your own code before requesting review

---

**Need help?** Check [CLAUDE.md](../../CLAUDE.md) for coding guidelines or [ARCHITECTURE.md](../../docs/ARCHITECTURE.md) for architecture patterns.
