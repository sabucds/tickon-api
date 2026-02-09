## 📋 Summary
<!-- Provide a concise description of what this PR does -->



## 🎯 Type of Change
<!-- Check the relevant option(s) -->

- [ ] ✨ **Feature** - New functionality
- [ ] 🐛 **Bug Fix** - Fixes an issue
- [ ] ♻️ **Refactor** - Code improvement without changing behavior
- [ ] 📝 **Documentation** - Documentation updates
- [ ] ✅ **Test** - Adding or updating tests
- [ ] 🔧 **Chore** - Build, CI, or tooling changes
- [ ] ⚡ **Performance** - Performance improvement
- [ ] 🔒 **Security** - Security enhancement

## 🏗️ Architecture Impact
<!-- Check all that apply -->

- [ ] 🔷 **Domain Layer** - Entities, Value Objects, Domain Events
- [ ] 🔶 **Application Layer** - Use Cases, Services, DTOs
- [ ] 🔸 **Infrastructure Layer** - Adapters, Repositories, Controllers
- [ ] 📦 **Common Module** - Shared abstractions
- [ ] 🔗 **Cross-Module Communication** - QueryBus, Events, Contracts
- [ ] 🗄️ **Database Schema** - Migration required
- [ ] 🔌 **API Contract** - Breaking/non-breaking API changes

## 🔍 Changes Made
<!-- Detailed list of changes -->

### Modified Services
<!-- Check affected services -->
- [ ] identity-service
- [ ] event-service
- [ ] api-gateway
- [ ] eureka-server
- [ ] common module

### Key Changes
<!-- Bullet points describing the changes -->
-
-
-

## 🧪 Testing Strategy
<!-- Describe how this was tested -->

### Unit Tests
- [ ] New unit tests added
- [ ] Existing tests updated
- [ ] All tests passing (`mvn test`)

### Integration Tests
- [ ] Integration tests added/updated
- [ ] Tested with other services
- [ ] Manual testing performed

### Test Coverage
```
Tests run: X
Failures: 0
Errors: 0
Coverage: X%
```

## 📸 Screenshots/Demo
<!-- If applicable, add screenshots or a demo video -->



## 🔗 Related Issues
<!-- Link related issues -->

Closes #
Relates to #

## 📚 Documentation
<!-- Check all that apply -->

- [ ] Code is self-documenting with clear names
- [ ] Complex logic has explanatory comments
- [ ] Public APIs have Javadoc
- [ ] Architecture docs updated (ARCHITECTURE.md)
- [ ] ADR created for architectural decisions
- [ ] README updated if needed
- [ ] CLAUDE.md updated if patterns changed

## ✅ Checklist
<!-- Ensure all items are checked before requesting review -->

### Code Quality
- [ ] Code follows project conventions (Hexagonal Architecture)
- [ ] Bounded contexts respected (no cross-module domain imports)
- [ ] Domain logic is in domain layer
- [ ] Infrastructure doesn't leak into domain
- [ ] DTOs used at boundaries
- [ ] Value objects used instead of primitives
- [ ] Exceptions are domain-specific

### Best Practices
- [ ] No code duplication (DRY)
- [ ] SOLID principles followed
- [ ] Dependencies point inward (Dependency Rule)
- [ ] Database transactions properly managed
- [ ] Proper error handling implemented
- [ ] Security considerations addressed
- [ ] No sensitive data in logs

### Git & CI
- [ ] Commits follow conventional commit standards
- [ ] Commits are atomic and logical
- [ ] No merge conflicts
- [ ] `mvn spotless:apply` applied
- [ ] `mvn verify` passes
- [ ] Branch is up to date with base branch

### Review Ready
- [ ] Self-reviewed the code
- [ ] Code is ready for production
- [ ] Breaking changes documented
- [ ] Migration steps documented (if needed)

## 🚀 Deployment Notes
<!-- Any special deployment considerations -->



## 📊 Performance Impact
<!-- If applicable, describe performance implications -->

- [ ] No performance impact
- [ ] Performance improved
- [ ] Performance regression (explained below)

<!-- If there's a performance change, provide details -->



## 🔐 Security Considerations
<!-- If applicable, describe security implications -->

- [ ] No security impact
- [ ] Security improved
- [ ] Needs security review

<!-- If there are security changes, provide details -->



## 💬 Additional Context
<!-- Any other context, considerations, or notes for reviewers -->



---

## 🎨 For Reviewers

### Focus Areas
<!-- What should reviewers pay special attention to? -->



### Questions for Reviewers
<!-- Any specific questions or concerns? -->



---

**Co-Authored-By:** Claude Sonnet 4.5 <noreply@anthropic.com>
🤖 _Generated with [Claude Code](https://claude.com/claude-code)_
