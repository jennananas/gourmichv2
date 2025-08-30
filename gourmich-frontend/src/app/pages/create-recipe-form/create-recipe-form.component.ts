import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl, FormArray  } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { TextareaModule } from 'primeng/textarea';
import { RatingModule } from 'primeng/rating';
import { KeyFilterModule } from 'primeng/keyfilter';
import { FieldsetModule } from 'primeng/fieldset';
import { StepperModule } from 'primeng/stepper';
import { CategoryService } from '../../services/category.service';
import { ChangeDetectorRef } from '@angular/core';
import { RecipeService } from '../../services/recipe.service';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-create-recipe-form',
  imports: [StepperModule,KeyFilterModule,RatingModule,TextareaModule,FormsModule,ButtonModule,
     ReactiveFormsModule, FloatLabelModule, InputText, AutoCompleteModule,
      SelectModule, InputNumberModule, CommonModule, 
      FieldsetModule],
  templateUrl: './create-recipe-form.component.html',
  styleUrl: './create-recipe-form.component.css'
})
export class CreateRecipeFormComponent implements OnInit {
  recipeForm!: FormGroup;

  selectedIngredientIndex: number | null = null;
  filteredIngredients: string[] = [];
  allPossibleIngredients: string[] = [
    'Tomato', 'Cheese', 'Flour', 'Milk', 'Sugar', 'Salt', 'Butter', 'Eggs', 'Vanilla', 'Chocolate'
  ];
  mode: 'create' | 'edit' = 'create';
  
  activeStep : number = 0;
  categories: { label: string; value: string }[] = [];
  recipeId?: number;
  
  ingredientInput = new FormControl('');
  quantityInput = new FormControl(1); 
  unitInput = new FormControl('');
  units: { label: string; value: string }[] = [
  { label: 'g', value: 'g' },
  { label: 'kg', value: 'kg' },
  { label: 'ml', value: 'ml' },
  { label: 'l', value: 'l' },
  { label: 'pcs', value: 'pcs' }
];

  constructor(private router: Router, private route: ActivatedRoute,
     private formBuilder: FormBuilder, private categoryService: CategoryService,
    private recipeService: RecipeService, private messageService: MessageService,
    private authService: AuthService,
     private cdRef: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe((categories: string[]) => {
      this.categories = categories.map(category => ({
        label: this.formatLabel(category), 
        value: category                    
      }));
    }); 
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.mode = 'edit';
      this.recipeId = Number(idParam);
      this.loadRecipeFromService(this.recipeId);
    } else {
      this.mode = 'create';
      this.initializeForm();
      this.activeStep = 1;
    }
  }

  private formatLabel(value: string) : string {
    return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  private loadRecipeFromService(recipeId: number): void {
    this.recipeService.getRecipeById(recipeId).subscribe(data => {
      this.initializeForm(data);
      this.activeStep = 1;
    });
  }

  private initializeForm(recipeData?: any): void {
    this.recipeForm = this.formBuilder.group({
      step1: this.formBuilder.group({
        title: [
          recipeData?.title || '', 
          [Validators.required, Validators.minLength(4), Validators.maxLength(30)]
        ],
        description: [
          recipeData?.description || '', 
          [Validators.pattern(/^[\p{L}0-9\s.,!?()'"«»;:\-’\n\r]*$/u)]
        ],
        imageUrl: [
          recipeData?.imageUrl || '', 
          [
            Validators.required,
            Validators.pattern(/^(https?:\/\/.*\.(?:png|jpg|jpeg|gif|webp))$/i),
          ]
        ],
        category: [recipeData?.category || '', [Validators.required]],
        difficulty: [recipeData?.difficulty || '', [Validators.required]],
        cookingTime: [
          recipeData?.cookingTime || 1, 
          [Validators.required, Validators.min(1), Validators.pattern(/^\d+(\.\d+)?$/)]
        ]
      }),
      step2: this.formBuilder.group({
        ingredients: this.formBuilder.array(
          recipeData?.ingredients?.map((ing: any) => this.formBuilder.group({
            name: [ing.name, [Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern(/^[\p{L}0-9\s.,!?()'"«»;:\-’]*$/u)]],
            quantity: [ing.quantity, [Validators.required, Validators.min(1), Validators.pattern(/^\d+(\.\d+)?$/)]],
            unit: [ing.unit, [Validators.required]]
          })) || [],
          [Validators.required, Validators.minLength(1)]
        )
      }),
      step3: this.formBuilder.group({
        instructions: [
          recipeData?.instructions || '', 
          [Validators.required, Validators.minLength(10), Validators.pattern(/^[\p{L}0-9\s.,!?()'"«»;:\-’\n\r]*$/u)]
        ]
      })
    });
    
    this.resetIngredientInputs();
  }

  /** Getters for form controls */  
  get step1(){ return this.recipeForm.get('step1') as FormGroup; }
  get step2(){ return this.recipeForm.get('step2') as FormGroup; }
  get step3(){ return this.recipeForm.get('step3') as FormGroup; }
  get ingredients(): FormArray {
    return this.step2.get('ingredients') as FormArray;
  }

  validateStep(stepForm : FormGroup): boolean {
    if (stepForm.invalid) {
      stepForm.markAllAsTouched();
      return false;
    }
    return true;
  }

  onNextStep(currentStepForm: FormGroup, nextStepIndex: number, activateCallback: Function) {
    if (!this.validateStep(currentStepForm)) {
      return;
    }
    activateCallback(nextStepIndex);
    this.cdRef.detectChanges();
  }

  onSubmit(): void {

    if (this.selectedIngredientIndex !== null) {
      const ingredientsArray = this.ingredients;
      const ingredientGroup = this.formBuilder.group({
        name: [this.ingredientInput.value, [Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern(/^[\p{L}0-9\s.,!?()'"«»;:\-’]*$/u)]], 
        quantity: [this.quantityInput.value, [Validators.required, Validators.min(1), Validators.pattern(/^\d+(\.\d+)?$/)]],
        unit: [this.unitInput.value, [Validators.required]]
      });
      ingredientsArray.setControl(this.selectedIngredientIndex, ingredientGroup);
      this.selectedIngredientIndex = null;
      this.resetIngredientInputs();
    }

    const recipeData = {
      ...this.step1.value,
      ...this.step2.value,
      ...this.step3.value,
      authorUsername: this.authService.getUsername()
    };

    if (this.mode === 'edit' && this.recipeId) {
      this.recipeService.updateRecipe(this.recipeId, recipeData).subscribe({
        next: (_) => {
          this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Recette mise à jour !' });
          this.router.navigate(['/recipe/' + this.recipeId]);
        },
        error: (_) => {
          this.messageService.add({ severity: 'error', summary: 'Erreur', detail: "Échec de la mise à jour de la recette." });
        }
      });
    } else {
      this.recipeService.createRecipe(recipeData).subscribe({
        next: (createdRecipe) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Recette ajoutée avec succès !'
          });
          this.router.navigate(['/recipes']);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: "Échec de l'enregistrement de la recette."
          });
        }
      });
    }
  }

  searchIngredients(event: any): void {
    const query = event.query.toLowerCase();
    this.filteredIngredients = this.allPossibleIngredients
      .filter(ingredient => ingredient.toLowerCase().includes(query));
  }

  addIngredient() {
    const name = this.ingredientInput.value;
    const quantity = this.quantityInput.value;
    const unit = this.unitInput.value;

    if (
      this.ingredientInput.invalid ||
      this.quantityInput.invalid ||
      this.unitInput.invalid ||
      !name || !quantity || !unit
    ) {
      this.ingredientInput.markAsTouched();
      this.quantityInput.markAsTouched();
      this.unitInput.markAsTouched();
      return;
    }

    const ingredientsArray = this.ingredients;

    const ingredientGroup = this.formBuilder.group({
      name: [name, [Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern(/^[\p{L}0-9\s.,!?()'"«»;:\-’]*$/u)]], 
      quantity: [quantity, [Validators.required, Validators.min(1), Validators.pattern(/^\d+(\.\d+)?$/)]],
      unit: [unit, [Validators.required]]
    });

    if (this.selectedIngredientIndex !== null) {
      ingredientsArray.setControl(this.selectedIngredientIndex, ingredientGroup);
      this.selectedIngredientIndex = null;
    } else {
      ingredientsArray.push(ingredientGroup);
    }

    this.resetIngredientInputs();

    ingredientsArray.markAsTouched();
    ingredientsArray.markAsDirty();
    ingredientsArray.updateValueAndValidity();
  }

  editIngredient(index: number): void {
    const ingredientGroup = this.ingredients.at(index) as FormGroup;

    this.ingredientInput.setValue(ingredientGroup.get('name')?.value);
    this.quantityInput.setValue(ingredientGroup.get('quantity')?.value);
    this.unitInput.setValue(ingredientGroup.get('unit')?.value ?? '');
    this.selectedIngredientIndex = index;
  }

  removeIngredient(index: number): void {
    const ingredientsArray = this.ingredients;
    ingredientsArray.removeAt(index);

    if (
      this.selectedIngredientIndex === index ||
      (this.selectedIngredientIndex !== null && this.selectedIngredientIndex >= ingredientsArray.length)
    ) {
      this.selectedIngredientIndex = null;
      this.resetIngredientInputs();
    }

    ingredientsArray.markAsTouched();
    ingredientsArray.markAsDirty();
    ingredientsArray.updateValueAndValidity();
  }

  resetIngredientInputs(): void {
    this.ingredientInput.reset();
    this.quantityInput.setValue(1);
    this.unitInput.reset();
  }
}
